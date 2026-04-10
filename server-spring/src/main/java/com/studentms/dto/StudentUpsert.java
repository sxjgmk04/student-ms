package com.studentms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record StudentUpsert(
    @NotBlank(message = "学号与姓名为必填") String studentId,
    @NotBlank(message = "学号与姓名为必填") String name,
    String gender,
    String major,
    @JsonProperty("class") String clazz,
    @Min(1900) @Max(2100) Integer grade,
    String status) {}
