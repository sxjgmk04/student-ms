package com.studentms.repo;

import com.studentms.entity.GraduationEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GraduationRepository extends JpaRepository<GraduationEntity, Long> {

  List<GraduationEntity> findAllByOrderByIdAsc();
}
