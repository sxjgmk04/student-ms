package com.studentms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.studentms.entity.UserEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserPayload(
    Long id,
    String username,
    String name,
    String role,
    String studentId,
    String email,
    String phone,
    String avatarUrl) {

  public static UserPayload from(UserEntity u) {
    return new UserPayload(
        u.getId(),
        u.getUsername(),
        u.getName(),
        u.getRole(),
        u.getStudentId(),
        u.getEmail(),
        u.getPhone(),
        u.getAvatarUrl());
  }
}
