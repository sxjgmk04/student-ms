package com.studentms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScoreCreateRequest(
    @NotBlank(message = "请填写完整成绩信息") String studentId,
    @NotBlank(message = "请填写完整成绩信息") String name,
    @NotBlank(message = "请填写完整成绩信息") String course,
    @NotBlank(message = "请填写完整成绩信息") String semester,
    @NotNull(message = "请填写完整成绩信息") @Min(0) @Max(150) Integer score) {}
