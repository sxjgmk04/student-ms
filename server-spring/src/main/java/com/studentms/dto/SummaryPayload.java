package com.studentms.dto;

import java.util.List;

public record SummaryPayload(
    List<StudentPayload> students,
    List<ChangePayload> changes,
    List<ScorePayload> scores,
    List<RewardPayload> rewards,
    List<GraduationPayload> graduation) {}
