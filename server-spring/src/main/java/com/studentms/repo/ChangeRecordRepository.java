package com.studentms.repo;

import com.studentms.entity.ChangeRecordEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeRecordRepository extends JpaRepository<ChangeRecordEntity, Long> {

  List<ChangeRecordEntity> findAllByOrderByIdDesc();

  List<ChangeRecordEntity> findByStudentIdOrderByIdDesc(String studentId);

  boolean existsByStudentIdAndStatus(String studentId, String status);

  long countByStatus(String status);

  long countByStudentIdAndStatus(String studentId, String status);
}
