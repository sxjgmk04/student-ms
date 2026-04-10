package com.studentms.dto;

public record AiStatusResponse(
    boolean ready,
    boolean enabled,
    String preset,
    String providerLabel,
    String effectiveBaseHost,
    String effectiveModel) {}
