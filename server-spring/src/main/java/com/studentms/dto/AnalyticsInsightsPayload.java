package com.studentms.dto;

import java.util.List;

public record AnalyticsInsightsPayload(
    String summary,
    List<String> highlights,
    List<RiskStudentItem> riskStudents,
    List<MajorStatItem> majorStats,
    int totalStudentsWithScores,
    int riskHighCount,
    int riskMediumCount,
    double highAvgThreshold,
    double mediumAvgThreshold,
    int failScoreLine,
    int highRiskFailCount,
    int mediumRiskFailCount,
    String riskModelDescription) {}
