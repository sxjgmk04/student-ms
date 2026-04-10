package com.studentms.service;

/**
 * 轻量学业风险预测：逻辑回归形式 p = σ(z)，特征均为可解释标量，便于论文说明「非黑箱」。
 *
 * <p>特征（均归一化到约 [0,1]）：均分偏低程度、挂科比例、学期趋势风险。系数为领域启发式标定，可替换为离线训练得到的权重。
 */
public final class RiskModelScorer {

  private RiskModelScorer() {}

  /**
   * @param avgScore 算术均分
   * @param failCount 不及格门数
   * @param trendLabel 学期趋势：上升 / 下降 / 平稳 / 样本不足
   * @return [0,1]，越大表示模型估计的学业风险越高
   */
  public static double probability(double avgScore, int failCount, String trendLabel) {
    double fAvg = clamp((100.0 - avgScore) / 38.0, 0.0, 1.0);
    double fFail = clamp(failCount / 3.0, 0.0, 1.0);
    double fTrend = trendFeature(trendLabel);

    // z = w0 + w·x
    double z = -0.85 + 2.15 * fAvg + 1.35 * fFail + 0.85 * fTrend;
    return sigmoid(z);
  }

  /** 三档，便于与规则引擎并列展示 */
  public static String riskLevelLabel(double p) {
    if (p >= 0.52) return "高";
    if (p >= 0.32) return "中";
    return "低";
  }

  public static String explainShort(
      double avgScore, int failCount, String trendLabel, double p) {
    return String.format(
        "均分偏低度×均分、挂科×门数、学期趋势「%s」→ 风险概率约 %.0f%%（逻辑回归式打分）",
        trendLabel == null ? "—" : trendLabel, p * 100.0);
  }

  private static double trendFeature(String trendLabel) {
    if (trendLabel == null) return 0.4;
    return switch (trendLabel) {
      case "下降" -> 1.0;
      case "上升" -> 0.12;
      case "平稳" -> 0.42;
      case "样本不足" -> 0.38;
      default -> 0.4;
    };
  }

  private static double sigmoid(double z) {
    if (z > 18) return 1.0;
    if (z < -18) return 0.0;
    return 1.0 / (1.0 + Math.exp(-z));
  }

  private static double clamp(double v, double lo, double hi) {
    return Math.max(lo, Math.min(hi, v));
  }
}
