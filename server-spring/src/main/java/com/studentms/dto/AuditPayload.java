package com.studentms.dto;

import com.studentms.entity.AuditLogEntity;

public record AuditPayload(
    Long id,
    Long operatorId,
    String operatorName,
    String operatorRole,
    String action,
    String detail,
    String ip,
    String operateTime) {

  public static AuditPayload from(AuditLogEntity e) {
    return new AuditPayload(
        e.getId(),
        e.getOperatorId(),
        e.getOperatorName(),
        e.getOperatorRole(),
        e.getAction(),
        e.getDetail(),
        e.getIp(),
        e.getOperateTime());
  }
}
