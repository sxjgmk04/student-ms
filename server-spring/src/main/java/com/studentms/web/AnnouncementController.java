package com.studentms.web;

import com.studentms.common.ApiResponse;
import com.studentms.dto.AnnouncementPayload;
import com.studentms.repo.AnnouncementRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnnouncementController {

  private final AnnouncementRepository announcementRepository;

  public AnnouncementController(AnnouncementRepository announcementRepository) {
    this.announcementRepository = announcementRepository;
  }

  @GetMapping("/api/announcements")
  public ApiResponse<List<AnnouncementPayload>> list() {
    List<AnnouncementPayload> rows =
        announcementRepository.findTop30ByOrderByCreatedAtDesc().stream()
            .map(AnnouncementPayload::from)
            .toList();
    return ApiResponse.ok(rows);
  }
}
