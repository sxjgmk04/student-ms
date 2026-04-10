package com.studentms.dto;

import java.util.List;

public record StudentInsightPayload(
    String studentId,
    String name,
    String major,
    double avgScore,
    int courseCount,
    int failCount,
    Double majorBenchmarkAvg,
    String vsMajorHint,
    String levelTag,
    String trendLabel,
    List<SemesterTrendItem> semesterTrends,
    List<String> tips,
    /** 逻辑回归式学业风险概率 [0,1]，与管理员端模型一致 */
    double modelRiskScore,
    String modelRiskHint) {}
