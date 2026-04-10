package com.studentms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "operator_id")
  private Long operatorId;

  @Column(name = "operator_name")
  private String operatorName;

  @Column(name = "operator_role")
  private String operatorRole;

  @Column(nullable = false)
  private String action;

  private String detail;

  private String ip;

  @Column(name = "operate_time", nullable = false)
  private String operateTime;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(Long operatorId) {
    this.operatorId = operatorId;
  }

  public String getOperatorName() {
    return operatorName;
  }

  public void setOperatorName(String operatorName) {
    this.operatorName = operatorName;
  }

  public String getOperatorRole() {
    return operatorRole;
  }

  public void setOperatorRole(String operatorRole) {
    this.operatorRole = operatorRole;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getOperateTime() {
    return operateTime;
  }

  public void setOperateTime(String operateTime) {
    this.operateTime = operateTime;
  }
}
