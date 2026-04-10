package com.studentms.web;

import com.studentms.common.ApiResponse;
import com.studentms.dto.AuditLogAppendRequest;
import com.studentms.dto.AuditPayload;
import com.studentms.entity.AuditLogEntity;
import com.studentms.repo.AuditLogRepository;
import com.studentms.security.AppUser;
import com.studentms.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuditController {

  private final AuditLogRepository auditLogRepository;
  private final AuditService auditService;

  public AuditController(AuditLogRepository auditLogRepository, AuditService auditService) {
    this.auditLogRepository = auditLogRepository;
    this.auditService = auditService;
  }

  @GetMapping("/api/audit-logs")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<List<AuditPayload>> list() {
    return ApiResponse.ok(
        auditLogRepository.findAllByOrderByIdDesc().stream().map(AuditPayload::from).toList());
  }

  @PostMapping("/api/audit-logs")
  public ApiResponse<AuditPayload> append(
      @Valid @RequestBody AuditLogAppendRequest body,
      @AuthenticationPrincipal AppUser user,
      HttpServletRequest req) {
    String detail = body.detail();
    auditService.insert(user, req, body.action(), detail != null ? detail : "");
    AuditLogEntity last = auditLogRepository.findFirstByOrderByIdDesc();
    return ApiResponse.ok(AuditPayload.from(last));
  }

  @DeleteMapping("/api/audit-logs")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Void> clear() {
    auditLogRepository.deleteAll();
    return ApiResponse.ok(null);
  }
}
