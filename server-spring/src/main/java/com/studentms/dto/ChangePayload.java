package com.studentms.dto;

import com.studentms.entity.ChangeRecordEntity;

public record ChangePayload(
    Long id,
    String studentId,
    String name,
    String type,
    String reason,
    String status,
    String date) {

  public static ChangePayload from(ChangeRecordEntity e) {
    return new ChangePayload(
        e.getId(),
        e.getStudentId(),
        e.getName(),
        e.getType(),
        e.getReason(),
        e.getStatus(),
        e.getDate());
  }
}
