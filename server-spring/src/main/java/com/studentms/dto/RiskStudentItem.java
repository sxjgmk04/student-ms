package com.studentms.dto;

public record RiskStudentItem(
    String studentId,
    String name,
    String major,
    double avgScore,
    int failCount,
    int courseCount,
    String riskLevel,
    String followUpStatus,
    String followUpNote,
    String followUpUpdatedAt,
    String triggerReason,
    String suggestion,
    double modelRiskScore,
    String modelRiskLevel,
    String modelExplanation) {}
