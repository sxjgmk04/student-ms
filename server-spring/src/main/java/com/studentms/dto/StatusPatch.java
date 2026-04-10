package com.studentms.dto;

import jakarta.validation.constraints.NotBlank;

public record StatusPatch(@NotBlank(message = "缺少状态") String status) {}
