package com.studentms.web;

import com.studentms.common.ApiException;
import com.studentms.common.ApiResponse;
import com.studentms.dto.StudentPayload;
import com.studentms.dto.StudentUpsert;
import com.studentms.entity.StudentEntity;
import com.studentms.repo.StudentRepository;
import com.studentms.security.AppUser;
import com.studentms.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ADMIN')")
public class StudentController {

  private final StudentRepository studentRepository;
  private final AuditService auditService;

  public StudentController(StudentRepository studentRepository, AuditService auditService) {
    this.studentRepository = studentRepository;
    this.auditService = auditService;
  }

  @GetMapping("/api/students")
  public ApiResponse<List<StudentPayload>> list() {
    return ApiResponse.ok(
        studentRepository.findAllByOrderByIdAsc().stream().map(StudentPayload::from).toList());
  }

  @PostMapping("/api/students")
  public ApiResponse<StudentPayload> create(
      @Valid @RequestBody StudentUpsert body,
      @AuthenticationPrincipal AppUser user,
      HttpServletRequest req) {
    if (studentRepository.findByStudentId(body.studentId()).isPresent()) {
      throw ApiException.badRequest("学号已存在");
    }
    StudentEntity e = new StudentEntity();
    e.setStudentId(body.studentId());
    e.setName(body.name());
    e.setGender(body.gender() != null ? body.gender() : "");
    e.setMajor(body.major() != null ? body.major() : "");
    e.setClassName(body.clazz() != null ? body.clazz() : "");
    e.setGrade(body.grade() != null ? body.grade() : 2024);
    e.setStatus(body.status() != null ? body.status() : "在读");
    studentRepository.save(e);
    auditService.insert(user, req, "新增学生信息", "学号：" + body.studentId() + "，姓名：" + body.name());
    return ApiResponse.ok(StudentPayload.from(e));
  }

  @PutMapping("/api/students/{id}")
  public ApiResponse<StudentPayload> update(
      @PathVariable long id,
      @Valid @RequestBody StudentUpsert body,
      @AuthenticationPrincipal AppUser user,
      HttpServletRequest req) {
    StudentEntity e =
        studentRepository.findById(id).orElseThrow(() -> ApiException.notFound("记录不存在"));
    studentRepository
        .findByStudentIdAndIdNot(body.studentId(), id)
        .ifPresent(
            x -> {
              throw ApiException.badRequest("学号已被其他记录使用");
            });
    e.setStudentId(body.studentId());
    e.setName(body.name());
    e.setGender(body.gender() != null ? body.gender() : "");
    e.setMajor(body.major() != null ? body.major() : "");
    e.setClassName(body.clazz() != null ? body.clazz() : "");
    e.setGrade(body.grade() != null ? body.grade() : 2024);
    e.setStatus(body.status() != null ? body.status() : "在读");
    studentRepository.save(e);
    auditService.insert(user, req, "编辑学生信息", "学号：" + e.getStudentId() + "，姓名：" + e.getName());
    return ApiResponse.ok(StudentPayload.from(e));
  }

  @DeleteMapping("/api/students/{id}")
  public ApiResponse<Void> delete(
      @PathVariable long id, @AuthenticationPrincipal AppUser user, HttpServletRequest req) {
    studentRepository.deleteById(id);
    auditService.insert(user, req, "删除学生信息", "删除了ID为" + id + "的学生数据");
    return ApiResponse.ok(null);
  }
}
