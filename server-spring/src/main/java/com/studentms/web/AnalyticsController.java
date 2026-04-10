package com.studentms.web;

import com.studentms.common.ApiException;
import com.studentms.common.ApiResponse;
import com.studentms.dto.AnalyticsInsightsPayload;
import com.studentms.dto.RiskFollowUpPayload;
import com.studentms.dto.RiskFollowUpUpsert;
import com.studentms.dto.StudentInsightPayload;
import com.studentms.security.AppUser;
import com.studentms.service.AuditService;
import com.studentms.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalyticsController {

  private final AnalyticsService analyticsService;
  private final AuditService auditService;

  public AnalyticsController(AnalyticsService analyticsService, AuditService auditService) {
    this.analyticsService = analyticsService;
    this.auditService = auditService;
  }

  @GetMapping("/api/analytics/insights")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<AnalyticsInsightsPayload> insights() {
    return ApiResponse.ok(analyticsService.buildInsights());
  }

  @GetMapping("/api/analytics/my-insight")
  @PreAuthorize("hasRole('STUDENT')")
  public ApiResponse<StudentInsightPayload> myInsight(@AuthenticationPrincipal AppUser user) {
    String sid = user.studentId();
    if (sid == null || sid.isBlank()) {
      throw ApiException.badRequest("当前账号未绑定学号");
    }
    return ApiResponse.ok(analyticsService.buildStudentInsight(sid));
  }

  @PatchMapping("/api/analytics/risk-follow-up/{studentId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<RiskFollowUpPayload> patchRiskFollowUp(
      @PathVariable String studentId,
      @Valid @RequestBody RiskFollowUpUpsert body,
      @AuthenticationPrincipal AppUser user,
      HttpServletRequest req) {
    var e = analyticsService.upsertRiskFollowUp(studentId, body.status(), body.note());
    auditService.insert(
        user,
        req,
        "学业风险跟进状态更新",
        "学号：" + studentId + "，跟进状态：" + e.getStatus() + (e.getNote().isBlank() ? "" : "，备注：" + e.getNote()));
    return ApiResponse.ok(RiskFollowUpPayload.from(e));
  }
}
