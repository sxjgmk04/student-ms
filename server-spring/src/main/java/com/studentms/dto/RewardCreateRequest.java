package com.studentms.dto;

import jakarta.validation.constraints.NotBlank;

public record RewardCreateRequest(
    @NotBlank(message = "请填写完整奖惩信息") String studentId,
    @NotBlank(message = "请填写完整奖惩信息") String name,
    @NotBlank(message = "请填写完整奖惩信息") String type,
    @NotBlank(message = "请填写完整奖惩信息") String title,
    @NotBlank(message = "请填写完整奖惩信息") String date) {}
