package com.studentms.repo;

import com.studentms.entity.ScoreEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreRepository extends JpaRepository<ScoreEntity, Long> {

  List<ScoreEntity> findAllByOrderByIdAsc();

  List<ScoreEntity> findByStudentIdOrderByIdAsc(String studentId);
}
