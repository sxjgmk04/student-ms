package com.studentms.dto;

import jakarta.validation.constraints.NotBlank;

public record PolicyQaRequest(@NotBlank(message = "请输入问题") String question) {}
