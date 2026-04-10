package com.studentms.service;

import com.studentms.common.ApiException;
import com.studentms.config.AnalyticsRuleProperties;
import com.studentms.dto.AnalyticsInsightsPayload;
import com.studentms.dto.MajorStatItem;
import com.studentms.dto.RiskStudentItem;
import com.studentms.dto.SemesterTrendItem;
import com.studentms.dto.StudentInsightPayload;
import com.studentms.entity.RiskFollowUpEntity;
import com.studentms.entity.ScoreEntity;
import com.studentms.entity.StudentEntity;
import com.studentms.repo.RiskFollowUpRepository;
import com.studentms.repo.ScoreRepository;
import com.studentms.repo.StudentRepository;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
  private static final String RISK_MODEL_DESCRIPTION =
      "对规则引擎检出的预警学生，叠加逻辑回归式风险概率 p∈[0,1]。"
          + "可解释特征：均分偏低程度、挂科门数（归一化）、学期趋势（上升/下降/平稳/样本不足）。"
          + "系数为可公开标定值，可替换为离线训练权重，便于与完全黑箱模型对比。";

  private static final String DEFAULT_FOLLOW_UP_STATUS = "待跟进";
  private static final DateTimeFormatter TIME =
      DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));

  private final ScoreRepository scoreRepository;
  private final StudentRepository studentRepository;
  private final RiskFollowUpRepository riskFollowUpRepository;
  private final AnalyticsRuleProperties analyticsRuleProperties;

  public AnalyticsService(
      ScoreRepository scoreRepository,
      StudentRepository studentRepository,
      RiskFollowUpRepository riskFollowUpRepository,
      AnalyticsRuleProperties analyticsRuleProperties) {
    this.scoreRepository = scoreRepository;
    this.studentRepository = studentRepository;
    this.riskFollowUpRepository = riskFollowUpRepository;
    this.analyticsRuleProperties = analyticsRuleProperties;
  }

  public AnalyticsInsightsPayload buildInsights() {
    List<ScoreEntity> scores = scoreRepository.findAllByOrderByIdAsc();
    List<StudentEntity> students = studentRepository.findAllByOrderByIdAsc();

    Map<String, StudentEntity> bySid =
        students.stream().collect(Collectors.toMap(StudentEntity::getStudentId, s -> s, (a, b) -> a));

    Map<String, List<ScoreEntity>> scoresByStudent =
        scores.stream().collect(Collectors.groupingBy(ScoreEntity::getStudentId));
    Map<String, RiskFollowUpEntity> followUpsByStudent =
        riskFollowUpRepository.findAll().stream()
            .collect(Collectors.toMap(RiskFollowUpEntity::getStudentId, f -> f, (a, b) -> b));

    List<RiskStudentItem> risks = new ArrayList<>();
    int high = 0;
    int medium = 0;

    for (Map.Entry<String, List<ScoreEntity>> e : scoresByStudent.entrySet()) {
      String sid = e.getKey();
      List<ScoreEntity> list = e.getValue();
      if (list.isEmpty()) continue;
      StudentEntity st = bySid.get(sid);
      if (st == null) continue;

      int n = list.size();
      int sum = list.stream().mapToInt(ScoreEntity::getScore).sum();
      double avg = sum / (double) n;
      long fails = list.stream().filter(s -> s.getScore() < analyticsRuleProperties.failScoreLine()).count();

      String level;
      if (avg < analyticsRuleProperties.highAvgThreshold()
          || fails >= analyticsRuleProperties.highRiskFailCount()) {
        level = "高";
        high++;
      } else if (avg < analyticsRuleProperties.mediumAvgThreshold()
          || fails >= analyticsRuleProperties.mediumRiskFailCount()) {
        level = "中";
        medium++;
      } else {
        continue;
      }

      String suggestion;
      if (avg < analyticsRuleProperties.highAvgThreshold()) {
        suggestion = "建议辅导员关注学业进度，可安排朋辈帮扶或重修规划。";
      } else if (fails >= analyticsRuleProperties.mediumRiskFailCount()) {
        suggestion = "存在不及格科目，建议及时补考/重修并加强平时考勤与作业。";
      } else {
        suggestion = "整体成绩偏低，建议关注学习方法与时间分配。";
      }
      String triggerReason =
          buildTriggerReason(avg, fails, analyticsRuleProperties.highAvgThreshold(), analyticsRuleProperties.mediumAvgThreshold());

      List<SemesterTrendItem> semesterTrendsForModel = buildSemesterTrends(list);
      String trendForModel = resolveTrendLabel(semesterTrendsForModel);
      double modelP = RiskModelScorer.probability(avg, (int) fails, trendForModel);
      String modelLevel = RiskModelScorer.riskLevelLabel(modelP);
      String modelExplain =
          RiskModelScorer.explainShort(avg, (int) fails, trendForModel, modelP);

      risks.add(
          new RiskStudentItem(
              sid,
              st.getName(),
              st.getMajor() != null ? st.getMajor() : "",
              Math.round(avg * 10) / 10.0,
              (int) fails,
              n,
              level,
              followUpByStudentId(sid, followUpsByStudent),
              followUpNoteByStudentId(sid, followUpsByStudent),
              followUpUpdatedAtByStudentId(sid, followUpsByStudent),
              triggerReason,
              suggestion,
              Math.round(modelP * 1000) / 1000.0,
              modelLevel,
              modelExplain));
    }

    risks.sort(
        Comparator.comparing((RiskStudentItem r) -> "高".equals(r.riskLevel()) ? 0 : 1)
            .thenComparingDouble(RiskStudentItem::avgScore));

    Map<String, List<Double>> majorToScores = new HashMap<>();
    for (StudentEntity st : students) {
      String major = st.getMajor() != null ? st.getMajor() : "未分类";
      List<ScoreEntity> scs = scoresByStudent.get(st.getStudentId());
      if (scs == null || scs.isEmpty()) continue;
      majorToScores.computeIfAbsent(major, k -> new ArrayList<>());
      for (ScoreEntity sc : scs) {
        majorToScores.get(major).add((double) sc.getScore());
      }
    }

    List<MajorStatItem> majorStats = new ArrayList<>();
    Map<String, Long> majorCounts =
        students.stream()
            .collect(
                Collectors.groupingBy(
                    s -> s.getMajor() != null ? s.getMajor() : "未分类", Collectors.counting()));

    for (Map.Entry<String, List<Double>> e : majorToScores.entrySet()) {
      String major = e.getKey();
      List<Double> vals = e.getValue();
      double avg = vals.stream().mapToDouble(Double::doubleValue).average().orElse(0);
      int cnt = majorCounts.getOrDefault(major, 0L).intValue();
      majorStats.add(new MajorStatItem(major, cnt, Math.round(avg * 10) / 10.0));
    }
    majorStats.sort(Comparator.comparing(MajorStatItem::avgScore).reversed());

    int withScores = scoresByStudent.size();
    List<String> highlights = new ArrayList<>();
    if (!majorStats.isEmpty()) {
      MajorStatItem top = majorStats.get(0);
      highlights.add(
          String.format(
              "专业均分领先：%s（约 %.1f 分，样本为该平台内成绩记录）",
              top.major(), top.avgScore() != null ? top.avgScore() : 0.0));
    }
    highlights.add(
        String.format(
            "当前规则引擎检出学业关注对象 %d 人（中风险 %d、高风险 %d），阈值可在后端调整。",
            high + medium, medium, high));
    highlights.add(
        String.format(
            "当前规则口径：高风险(均分<%.1f 或 挂科>=%d)，中风险(均分<%.1f 或 挂科>=%d)。",
            analyticsRuleProperties.highAvgThreshold(),
            analyticsRuleProperties.highRiskFailCount(),
            analyticsRuleProperties.mediumAvgThreshold(),
            analyticsRuleProperties.mediumRiskFailCount()));
    highlights.add(
        "预警名单已叠加轻量逻辑回归式风险概率（与学期趋势等特征结合），可与规则结果交叉验证。");

    String summary =
        String.format(
            "系统基于「均分阈值 + 不及格门数」对 %d 名有成绩记录的学生进行扫描，"
                + "并对预警对象给出可解释的逻辑回归式风险概率；"
                + "规则与统计模型并列，便于答辩中对比「白盒规则」与「概率预测」的差异。",
            withScores);

    return new AnalyticsInsightsPayload(
        summary,
        highlights,
        risks,
        majorStats,
        withScores,
        high,
        medium,
        analyticsRuleProperties.highAvgThreshold(),
        analyticsRuleProperties.mediumAvgThreshold(),
        analyticsRuleProperties.failScoreLine(),
        analyticsRuleProperties.highRiskFailCount(),
        analyticsRuleProperties.mediumRiskFailCount(),
        RISK_MODEL_DESCRIPTION);
  }

  public StudentInsightPayload buildStudentInsight(String studentId) {
    StudentEntity st =
        studentRepository
            .findByStudentId(studentId)
            .orElseThrow(() -> ApiException.notFound("未找到学籍信息"));
    List<ScoreEntity> mine = scoreRepository.findByStudentIdOrderByIdAsc(studentId);
    String major = st.getMajor() != null ? st.getMajor() : "未分类";

    if (mine.isEmpty()) {
      return new StudentInsightPayload(
          studentId,
          st.getName(),
          major,
          0,
          0,
          0,
          null,
          "本专业暂无成绩样本或您尚未录入成绩。",
          "无数据",
          "无趋势",
          List.of(),
          List.of("请登录教务系统核对成绩是否已同步，或联系管理员。"),
          0.0,
          "无成绩记录，未计算模型风险分。");
    }

    int n = mine.size();
    int sum = mine.stream().mapToInt(ScoreEntity::getScore).sum();
    double avg = sum / (double) n;
    long fails = mine.stream().filter(s -> s.getScore() < analyticsRuleProperties.failScoreLine()).count();
    double rounded = Math.round(avg * 10) / 10.0;

    Optional<Double> majorAvgOpt = computeMajorBenchmarkAvg(major);
    String vsHint;
    if (majorAvgOpt.isEmpty()) {
      vsHint = "本专业暂无可比成绩样本。";
    } else {
      double mavg = majorAvgOpt.get();
      double delta = rounded - mavg;
      if (Math.abs(delta) < 0.05) {
        vsHint = String.format("您的平均分与本专业整体均分（约 %.1f）接近。", mavg);
      } else if (delta > 0) {
        vsHint = String.format("您的平均分高于本专业整体均分（约 %.1f）约 %.1f 分。", mavg, delta);
      } else {
        vsHint = String.format("您的平均分低于本专业整体均分（约 %.1f）约 %.1f 分。", mavg, -delta);
      }
    }

    String level;
    if (rounded < analyticsRuleProperties.highAvgThreshold()
        || fails >= analyticsRuleProperties.highRiskFailCount()) {
      level = "预警";
    } else if (rounded < analyticsRuleProperties.mediumAvgThreshold()
        || fails >= analyticsRuleProperties.mediumRiskFailCount()) {
      level = "关注";
    } else {
      level = "稳定";
    }

    List<SemesterTrendItem> semesterTrends = buildSemesterTrends(mine);
    String trendLabel = resolveTrendLabel(semesterTrends);

    List<String> tips = new ArrayList<>();
    tips.add(
        String.format(
            "已修 %d 门课程，算术平均分 %.1f，不及格科目 %d 门。", n, rounded, fails));
    if (fails > 0) {
      tips.add("建议优先处理不及格科目的补考或重修计划，并关注平时成绩构成。");
    }
    if (rounded >= 85) {
      tips.add("整体表现良好，可保持学习节奏并适当拓展竞赛或实践。");
    } else if (level.equals("关注")) {
      tips.add("建议对照培养方案检查学分进度，必要时向任课教师或辅导员咨询。");
    } else if (level.equals("预警")) {
      tips.add("建议制定阶段性学习目标，合理利用自习与答疑资源。");
    }
    if ("上升".equals(trendLabel)) {
      tips.add("近学期成绩呈上升趋势，可继续保持当前学习节奏。");
    } else if ("下降".equals(trendLabel)) {
      tips.add("近学期成绩有下降趋势，建议尽快复盘薄弱课程并调整计划。");
    }

    double modelP = RiskModelScorer.probability(rounded, (int) fails, trendLabel);
    String modelHint =
        String.format(
            "模型估计学业风险概率约 %.0f%%（均分、挂科与学期趋势综合，与管理员端分析一致）。",
            modelP * 100.0);

    return new StudentInsightPayload(
        studentId,
        st.getName(),
        major,
        rounded,
        n,
        (int) fails,
        majorAvgOpt.orElse(null),
        vsHint,
        level,
        trendLabel,
        semesterTrends,
        tips,
        Math.round(modelP * 1000) / 1000.0,
        modelHint);
  }

  private Optional<Double> computeMajorBenchmarkAvg(String major) {
    List<StudentEntity> students = studentRepository.findAllByOrderByIdAsc();
    List<ScoreEntity> allScores = scoreRepository.findAllByOrderByIdAsc();
    Map<String, List<ScoreEntity>> byStudent =
        allScores.stream().collect(Collectors.groupingBy(ScoreEntity::getStudentId));

    List<Double> vals = new ArrayList<>();
    for (StudentEntity s : students) {
      String m = s.getMajor() != null ? s.getMajor() : "未分类";
      if (!m.equals(major)) continue;
      List<ScoreEntity> scs = byStudent.get(s.getStudentId());
      if (scs == null) continue;
      for (ScoreEntity sc : scs) {
        vals.add((double) sc.getScore());
      }
    }
    if (vals.isEmpty()) return Optional.empty();
    double avg = vals.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    return Optional.of(Math.round(avg * 10) / 10.0);
  }

  private String buildTriggerReason(double avg, long fails, double highAvg, double mediumAvg) {
    List<String> reasons = new ArrayList<>();
    if (avg < highAvg) {
      reasons.add(String.format("均分 %.1f 低于高风险阈值 %.1f", avg, highAvg));
    } else if (avg < mediumAvg) {
      reasons.add(String.format("均分 %.1f 低于中风险阈值 %.1f", avg, mediumAvg));
    }
    if (fails >= analyticsRuleProperties.highRiskFailCount()) {
      reasons.add(
          String.format(
              "挂科 %d 门达到高风险阈值 %d 门",
              fails, analyticsRuleProperties.highRiskFailCount()));
    } else if (fails >= analyticsRuleProperties.mediumRiskFailCount()) {
      reasons.add(
          String.format(
              "挂科 %d 门达到中风险阈值 %d 门",
              fails, analyticsRuleProperties.mediumRiskFailCount()));
    }
    if (reasons.isEmpty()) {
      return "触发条件未命中（规则边界误差）";
    }
    return String.join("；", reasons);
  }

  public RiskFollowUpEntity upsertRiskFollowUp(String studentId, String status, String note) {
    String safeStudentId = studentId == null ? "" : studentId.trim();
    if (safeStudentId.isBlank()) {
      throw ApiException.badRequest("学号不能为空");
    }
    String safeStatus = status == null ? "" : status.trim();
    if (safeStatus.isBlank()) {
      throw ApiException.badRequest("跟进状态不能为空");
    }
    RiskFollowUpEntity entity =
        riskFollowUpRepository.findByStudentId(safeStudentId).orElseGet(RiskFollowUpEntity::new);
    entity.setStudentId(safeStudentId);
    entity.setStatus(safeStatus);
    entity.setNote(note != null ? note.trim() : "");
    entity.setUpdatedAt(TIME.format(ZonedDateTime.now()));
    return riskFollowUpRepository.save(entity);
  }

  private String followUpByStudentId(String studentId, Map<String, RiskFollowUpEntity> followUpsByStudent) {
    RiskFollowUpEntity e = followUpsByStudent.get(studentId);
    if (e == null || e.getStatus() == null || e.getStatus().isBlank()) {
      return DEFAULT_FOLLOW_UP_STATUS;
    }
    return e.getStatus();
  }

  private String followUpNoteByStudentId(
      String studentId, Map<String, RiskFollowUpEntity> followUpsByStudent) {
    RiskFollowUpEntity e = followUpsByStudent.get(studentId);
    if (e == null || e.getNote() == null) return "";
    return e.getNote();
  }

  private String followUpUpdatedAtByStudentId(
      String studentId, Map<String, RiskFollowUpEntity> followUpsByStudent) {
    RiskFollowUpEntity e = followUpsByStudent.get(studentId);
    if (e == null || e.getUpdatedAt() == null) return "";
    return e.getUpdatedAt();
  }

  private List<SemesterTrendItem> buildSemesterTrends(List<ScoreEntity> mine) {
    Map<String, List<ScoreEntity>> bySemester =
        mine.stream().collect(Collectors.groupingBy(s -> s.getSemester() != null ? s.getSemester() : "未知学期"));
    return bySemester.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(
            e -> {
              List<ScoreEntity> list = e.getValue();
              int count = list.size();
              int sum = list.stream().mapToInt(ScoreEntity::getScore).sum();
              int failCount =
                  (int)
                      list.stream()
                          .filter(sc -> sc.getScore() < analyticsRuleProperties.failScoreLine())
                          .count();
              double avg = count == 0 ? 0 : (sum / (double) count);
              return new SemesterTrendItem(e.getKey(), Math.round(avg * 10) / 10.0, failCount, count);
            })
        .toList();
  }

  private String resolveTrendLabel(List<SemesterTrendItem> trends) {
    if (trends.size() < 2) return "样本不足";
    double first = trends.get(0).avgScore();
    double last = trends.get(trends.size() - 1).avgScore();
    double delta = last - first;
    if (delta >= 5) return "上升";
    if (delta <= -5) return "下降";
    return "平稳";
  }
}
