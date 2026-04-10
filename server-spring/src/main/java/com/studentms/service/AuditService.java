package com.studentms.service;

import com.studentms.entity.AuditLogEntity;
import com.studentms.repo.AuditLogRepository;
import com.studentms.security.AppUser;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

  private static final DateTimeFormatter TIME =
      DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));

  private final AuditLogRepository auditLogRepository;

  public AuditService(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  @Transactional
  public void insert(AppUser user, HttpServletRequest req, String action, String detail) {
    AuditLogEntity e = new AuditLogEntity();
    e.setOperatorId(user != null ? user.id() : null);
    e.setOperatorName(user != null ? user.name() : null);
    e.setOperatorRole(user != null ? user.role() : null);
    e.setAction(action);
    e.setDetail(detail != null ? detail : "");
    e.setIp(clientIp(req));
    e.setOperateTime(TIME.format(ZonedDateTime.now()));
    auditLogRepository.save(e);
  }

  private static String clientIp(HttpServletRequest req) {
    if (req == null) return "";
    String x = req.getHeader("X-Forwarded-For");
    if (x != null && !x.isBlank()) return x.split(",")[0].trim();
    return req.getRemoteAddr() != null ? req.getRemoteAddr() : "";
  }
}
