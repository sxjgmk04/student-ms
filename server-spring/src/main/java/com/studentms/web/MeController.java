package com.studentms.web;

import com.studentms.common.ApiResponse;
import com.studentms.dto.TodoSummary;
import com.studentms.repo.ChangeRecordRepository;
import com.studentms.security.AppUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

  private final ChangeRecordRepository changeRecordRepository;

  public MeController(ChangeRecordRepository changeRecordRepository) {
    this.changeRecordRepository = changeRecordRepository;
  }

  @GetMapping("/api/me/todos")
  public ApiResponse<TodoSummary> todos(@AuthenticationPrincipal AppUser user) {
    long pending;
    if (user.isAdmin()) {
      pending = changeRecordRepository.countByStatus("审核中");
    } else {
      String sid = user.studentId();
      pending =
          (sid == null || sid.isBlank())
              ? 0
              : changeRecordRepository.countByStudentIdAndStatus(sid.trim(), "审核中");
    }
    return ApiResponse.ok(new TodoSummary(pending));
  }
}
