package com.studentms.dto;

import jakarta.validation.constraints.NotBlank;

public record AiModelSwitchRequest(
    @NotBlank(message = "请选择要切换的模型") String optionKey) {}
