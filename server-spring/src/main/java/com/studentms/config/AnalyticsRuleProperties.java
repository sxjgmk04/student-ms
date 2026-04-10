package com.studentms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.analytics.rules")
public record AnalyticsRuleProperties(
    double highAvgThreshold,
    double mediumAvgThreshold,
    int failScoreLine,
    int highRiskFailCount,
    int mediumRiskFailCount) {}
