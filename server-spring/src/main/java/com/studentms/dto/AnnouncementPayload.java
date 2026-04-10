package com.studentms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.studentms.entity.AnnouncementEntity;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AnnouncementPayload(Long id, String title, String content, String createdAt) {

  private static final DateTimeFormatter FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

  public static AnnouncementPayload from(AnnouncementEntity e) {
    String at = e.getCreatedAt() != null ? FMT.format(e.getCreatedAt()) : null;
    return new AnnouncementPayload(e.getId(), e.getTitle(), e.getContent(), at);
  }
}
