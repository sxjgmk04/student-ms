package com.studentms.dto;

import jakarta.validation.constraints.NotBlank;

public record RiskFollowUpUpsert(
    @NotBlank(message = "跟进状态不能为空") String status, String note) {}
