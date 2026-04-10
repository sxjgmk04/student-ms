package com.studentms.repo;

import com.studentms.entity.AnnouncementEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<AnnouncementEntity, Long> {

  List<AnnouncementEntity> findTop30ByOrderByCreatedAtDesc();
}
