package com.studentms.web;

import com.studentms.common.ApiException;
import com.studentms.common.ApiResponse;
import com.studentms.dto.GraduationPayload;
import com.studentms.dto.StatusPatch;
import com.studentms.entity.GraduationEntity;
import com.studentms.repo.GraduationRepository;
import com.studentms.security.AppUser;
import com.studentms.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ADMIN')")
public class GraduationController {

  private final GraduationRepository graduationRepository;
  private final AuditService auditService;

  public GraduationController(
      GraduationRepository graduationRepository, AuditService auditService) {
    this.graduationRepository = graduationRepository;
    this.auditService = auditService;
  }

  @GetMapping("/api/graduation")
  public ApiResponse<List<GraduationPayload>> list() {
    return ApiResponse.ok(
        graduationRepository.findAllByOrderByIdAsc().stream()
            .map(GraduationPayload::from)
            .toList());
  }

  @PatchMapping("/api/graduation/{id}")
  public ApiResponse<GraduationPayload> patch(
      @PathVariable long id,
      @Valid @RequestBody StatusPatch body,
      @AuthenticationPrincipal AppUser user,
      HttpServletRequest req) {
    GraduationEntity e =
        graduationRepository.findById(id).orElseThrow(() -> ApiException.notFound("记录不存在"));
    e.setStatus(body.status());
    graduationRepository.save(e);
    auditService.insert(
        user,
        req,
        "毕业审核" + body.status(),
        "学号：" + e.getStudentId() + "，姓名：" + e.getName() + "，专业：" + e.getMajor());
    return ApiResponse.ok(GraduationPayload.from(e));
  }
}
