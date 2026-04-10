package com.studentms.web;

import com.studentms.common.ApiException;
import com.studentms.common.ApiResponse;
import com.studentms.dto.StudentPayload;
import com.studentms.entity.StudentEntity;
import com.studentms.repo.StudentRepository;
import com.studentms.security.AppUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentSelfController {

  private final StudentRepository studentRepository;

  public StudentSelfController(StudentRepository studentRepository) {
    this.studentRepository = studentRepository;
  }

  /** 学生登录后读取本人学籍档案（专业、班级等），用于成绩单等页面展示。 */
  @GetMapping("/api/students/me")
  @PreAuthorize("hasRole('STUDENT')")
  public ApiResponse<StudentPayload> myProfile(@AuthenticationPrincipal AppUser user) {
    String sid = user.studentId();
    if (sid == null || sid.isBlank()) {
      throw ApiException.badRequest("账号未绑定学号");
    }
    StudentEntity e =
        studentRepository
            .findByStudentId(sid)
            .orElseThrow(() -> ApiException.notFound("未找到与当前账号对应的学籍记录"));
    return ApiResponse.ok(StudentPayload.from(e));
  }
}
