package com.studentms.repo;

import com.studentms.entity.RiskFollowUpEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskFollowUpRepository extends JpaRepository<RiskFollowUpEntity, Long> {
  Optional<RiskFollowUpEntity> findByStudentId(String studentId);
}
