package com.studentms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.studentms.entity.StudentEntity;

public record StudentPayload(
    Long id,
    String studentId,
    String name,
    String gender,
    String major,
    @JsonProperty("class") String clazz,
    Integer grade,
    String status,
    String dataSource) {

  public static StudentPayload from(StudentEntity e) {
    return new StudentPayload(
        e.getId(),
        e.getStudentId(),
        e.getName(),
        e.getGender(),
        e.getMajor(),
        e.getClassName(),
        e.getGrade(),
        e.getStatus(),
        e.getDataSource());
  }
}
