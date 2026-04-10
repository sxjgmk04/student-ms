package com.studentms.repo;

import com.studentms.entity.RewardEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardRepository extends JpaRepository<RewardEntity, Long> {

  List<RewardEntity> findAllByOrderByIdAsc();

  List<RewardEntity> findByStudentIdOrderByIdAsc(String studentId);
}
