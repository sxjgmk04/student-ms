package com.studentms.dto;

import java.util.List;

public record AiModelConfigResponse(
    String currentKey, String currentLabel, String currentPreset, String currentModel, List<AiModelOption> options) {}

