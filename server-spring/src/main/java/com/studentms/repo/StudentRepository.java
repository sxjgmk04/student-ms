package com.studentms.repo;

import com.studentms.entity.StudentEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<StudentEntity, Long> {

  Optional<StudentEntity> findByStudentId(String studentId);

  Optional<StudentEntity> findByStudentIdAndIdNot(String studentId, Long id);

  List<StudentEntity> findAllByOrderByIdAsc();
}
