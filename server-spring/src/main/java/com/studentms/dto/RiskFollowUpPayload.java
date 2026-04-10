package com.studentms.dto;

import com.studentms.entity.RiskFollowUpEntity;

public record RiskFollowUpPayload(String studentId, String status, String note, String updatedAt) {
  public static RiskFollowUpPayload from(RiskFollowUpEntity e) {
    return new RiskFollowUpPayload(e.getStudentId(), e.getStatus(), e.getNote(), e.getUpdatedAt());
  }
}
