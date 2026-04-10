package com.studentms.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeAssistRequest(@NotBlank(message = "请描述你的情况或诉求") String description) {}
