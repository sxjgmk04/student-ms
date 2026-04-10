package com.studentms.web;

import com.studentms.common.ApiResponse;
import com.studentms.dto.ScoreCreateRequest;
import com.studentms.dto.ScorePayload;
import com.studentms.entity.ScoreEntity;
import com.studentms.repo.ScoreRepository;
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
public class ScoreController {

  private final ScoreRepository scoreRepository;
  private final AuditService auditService;

  public ScoreController(ScoreRepository scoreRepository, AuditService auditService) {
    this.scoreRepository = scoreRepository;
    this.auditService = auditService;
  }

  @GetMapping("/api/scores")
  public ApiResponse<List<ScorePayload>> list(@AuthenticationPrincipal AppUser user) {
    List<ScoreEntity> rows =
        user.isAdmin()
            ? scoreRepository.findAllByOrderByIdAsc()
            : scoreRepository.findByStudentIdOrderByIdAsc(user.studentId());
    return ApiResponse.ok(rows.stream().map(ScorePayload::from).toList());
  }

  @PostMapping("/api/scores")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<ScorePayload> create(
      @Valid @RequestBody ScoreCreateRequest body,
      @AuthenticationPrincipal AppUser user,
      HttpServletRequest req) {
    int scoreVal = body.score();
    ScoreEntity e = new ScoreEntity();
    e.setStudentId(body.studentId());
    e.setName(body.name());
    e.setCourse(body.course());
    e.setScore(scoreVal);
    e.setSemester(body.semester());
    scoreRepository.save(e);
    auditService.insert(
        user,
        req,
        "录入学生成绩",
        "学号：" + body.studentId() + "，课程：" + body.course() + "，分数：" + scoreVal);
    return ApiResponse.ok(ScorePayload.from(e));
  }
}
