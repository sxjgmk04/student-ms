package com.studentms.repo;

import com.studentms.entity.AuditLogEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

  List<AuditLogEntity> findAllByOrderByIdDesc();

  AuditLogEntity findFirstByOrderByIdDesc();
}
