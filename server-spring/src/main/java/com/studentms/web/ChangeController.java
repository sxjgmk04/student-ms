package com.studentms.web;

import com.studentms.common.ApiException;
import com.studentms.common.ApiResponse;
import com.studentms.dto.ChangePayload;
import com.studentms.dto.StatusPatch;
import com.studentms.entity.ChangeRecordEntity;
import com.studentms.repo.ChangeRecordRepository;
import com.studentms.security.AppUser;
import com.studentms.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChangeController {

  private final ChangeRecordRepository changeRecordRepository;
  private final AuditService auditService;

  public ChangeController(
      ChangeRecordRepository changeRecordRepository, AuditService auditService) {
    this.changeRecordRepository = changeRecordRepository;
    this.auditService = auditService;
  }

  @GetMapping("/api/changes")
  public ApiResponse<List<ChangePayload>> list(@AuthenticationPrincipal AppUser user) {
    List<ChangeRecordEntity> rows =
        user.isAdmin()
            ? changeRecordRepository.findAllByOrderByIdDesc()
            : changeRecordRepository.findByStudentIdOrderByIdDesc(user.studentId());
    return ApiResponse.ok(rows.stream().map(ChangePayload::from).toList());
  }

  @PostMapping("/api/changes")
  @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
  public ApiResponse<ChangePayload> create(
      @RequestBody Map<String, Object> body,
      @AuthenticationPrincipal AppUser user,
      HttpServletRequest req) {
    if (user.isAdmin()) {
      return ApiResponse.ok(createAsAdmin(body, user, req));
    }
    return ApiResponse.ok(createAsStudent(body, user, req));
  }

  private ChangePayload createAsAdmin(
      Map<String, Object> body, AppUser user, HttpServletRequest req) {
    String studentId = str(body, "studentId");
    String name = str(body, "name");
    String type = str(body, "type");
    if (studentId == null || name == null || type == null
        || studentId.isBlank() || name.isBlank() || type.isBlank()) {
      throw ApiException.badRequest("请填写完整异动信息");
    }
    String reason = str(body, "reason");
    String status = str(body, "status");
    String date = str(body, "date");
    if (date == null || date.isBlank()) {
      date = LocalDate.now().toString();
    }
    ChangeRecordEntity e = new ChangeRecordEntity();
    e.setStudentId(studentId.trim());
    e.setName(name.trim());
    e.setType(type.trim());
    e.setReason(reason != null ? reason : "");
    e.setStatus(status != null && !status.isBlank() ? status : "审核中");
    e.setDate(date);
    changeRecordRepository.save(e);
    auditService.insert(user, req, "新增学籍异动申请", "学号：" + studentId + "，异动类型：" + type);
    return ChangePayload.from(e);
  }

  private ChangePayload createAsStudent(
      Map<String, Object> body, AppUser user, HttpServletRequest req) {
    String sid = user.studentId();
    if (sid == null || sid.isBlank()) {
      throw ApiException.badRequest("当前账号未绑定学号，无法提交申请");
    }
    String displayName = user.name();
    if (displayName == null || displayName.isBlank()) {
      throw ApiException.badRequest("当前账号缺少姓名信息，无法提交申请");
    }
    if (changeRecordRepository.existsByStudentIdAndStatus(sid.trim(), "审核中")) {
      throw ApiException.badRequest("您已有审核中的异动申请，请等待审核结果后再提交");
    }
    String type = str(body, "type");
    if (type == null || type.isBlank()) {
      throw ApiException.badRequest("请选择异动类型");
    }
    String reason = str(body, "reason");
    if (reason == null || reason.isBlank()) {
      throw ApiException.badRequest("请填写申请原因");
    }
    String date = str(body, "date");
    if (date == null || date.isBlank()) {
      date = LocalDate.now().toString();
    }
    ChangeRecordEntity e = new ChangeRecordEntity();
    e.setStudentId(sid.trim());
    e.setName(displayName.trim());
    e.setType(type.trim());
    e.setReason(reason.trim());
    e.setStatus("审核中");
    e.setDate(date);
    changeRecordRepository.save(e);
    auditService.insert(
        user, req, "提交学籍异动申请", "学号：" + sid + "，异动类型：" + type);
    return ChangePayload.from(e);
  }

  @PatchMapping("/api/changes/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<ChangePayload> patchStatus(
      @PathVariable long id,
      @Valid @RequestBody StatusPatch body,
      @AuthenticationPrincipal AppUser user,
      HttpServletRequest req) {
    ChangeRecordEntity e =
        changeRecordRepository.findById(id).orElseThrow(() -> ApiException.notFound("记录不存在"));
    e.setStatus(body.status());
    changeRecordRepository.save(e);
    auditService.insert(
        user,
        req,
        "学籍异动审核" + body.status(),
        "学号：" + e.getStudentId() + "，姓名：" + e.getName() + "，异动类型：" + e.getType());
    return ApiResponse.ok(ChangePayload.from(e));
  }

  private static String str(Map<String, Object> m, String k) {
    if (m == null) return null;
    Object v = m.get(k);
    return v == null ? null : String.valueOf(v);
  }
}
