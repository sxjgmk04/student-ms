package com.studentms.web;

import com.studentms.common.ApiResponse;
import com.studentms.dto.RewardCreateRequest;
import com.studentms.dto.RewardPayload;
import com.studentms.entity.RewardEntity;
import com.studentms.repo.RewardRepository;
import com.studentms.security.AppUser;
import com.studentms.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RewardController {

  private final RewardRepository rewardRepository;
  private final AuditService auditService;

  public RewardController(RewardRepository rewardRepository, AuditService auditService) {
    this.rewardRepository = rewardRepository;
    this.auditService = auditService;
  }

  @GetMapping("/api/rewards")
  public ApiResponse<List<RewardPayload>> list(@AuthenticationPrincipal AppUser user) {
    List<RewardEntity> rows =
        user.isAdmin()
            ? rewardRepository.findAllByOrderByIdAsc()
            : rewardRepository.findByStudentIdOrderByIdAsc(user.studentId());
    return ApiResponse.ok(rows.stream().map(RewardPayload::from).toList());
  }

  @PostMapping("/api/rewards")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<RewardPayload> create(
      @Valid @RequestBody RewardCreateRequest body,
      @AuthenticationPrincipal AppUser user,
      HttpServletRequest req) {
    RewardEntity e = new RewardEntity();
    e.setStudentId(body.studentId());
    e.setName(body.name());
    e.setType(body.type());
    e.setTitle(body.title());
    e.setDate(body.date());
    rewardRepository.save(e);
    auditService.insert(
        user, req, "新增" + body.type() + "记录", "学号：" + body.studentId() + "，内容：" + body.title());
    return ApiResponse.ok(RewardPayload.from(e));
  }
}
