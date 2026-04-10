package com.studentms.dto;

import jakarta.validation.constraints.NotBlank;

public record AuditLogAppendRequest(
    @NotBlank(message = "缺少操作类型") String action, String detail) {}
