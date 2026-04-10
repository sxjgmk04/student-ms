package com.studentms.dto;

import com.studentms.entity.GraduationEntity;

public record GraduationPayload(
    Long id, String studentId, String name, String major, String status, Integer thesisScore) {

  public static GraduationPayload from(GraduationEntity e) {
    return new GraduationPayload(
        e.getId(),
        e.getStudentId(),
        e.getName(),
        e.getMajor(),
        e.getStatus(),
        e.getThesisScore());
  }
}
