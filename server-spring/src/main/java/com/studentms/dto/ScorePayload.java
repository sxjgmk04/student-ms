package com.studentms.dto;

import com.studentms.entity.ScoreEntity;

public record ScorePayload(
    Long id, String studentId, String name, String course, Integer score, String semester) {

  public static ScorePayload from(ScoreEntity e) {
    return new ScorePayload(
        e.getId(), e.getStudentId(), e.getName(), e.getCourse(), e.getScore(), e.getSemester());
  }
}
