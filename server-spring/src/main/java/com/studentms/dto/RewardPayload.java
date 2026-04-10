package com.studentms.dto;

import com.studentms.entity.RewardEntity;

public record RewardPayload(
    Long id, String studentId, String name, String type, String title, String date) {

  public static RewardPayload from(RewardEntity e) {
    return new RewardPayload(
        e.getId(), e.getStudentId(), e.getName(), e.getType(), e.getTitle(), e.getDate());
  }
}
