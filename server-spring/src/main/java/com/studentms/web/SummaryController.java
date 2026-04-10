package com.studentms.web;

import com.studentms.common.ApiResponse;
import com.studentms.dto.ChangePayload;
import com.studentms.dto.GraduationPayload;
import com.studentms.dto.RewardPayload;
import com.studentms.dto.ScorePayload;
import com.studentms.dto.StudentPayload;
import com.studentms.dto.SummaryPayload;
import com.studentms.repo.ChangeRecordRepository;
import com.studentms.repo.GraduationRepository;
import com.studentms.repo.RewardRepository;
import com.studentms.repo.ScoreRepository;
import com.studentms.repo.StudentRepository;
import com.studentms.security.AppUser;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SummaryController {

  private final StudentRepository studentRepository;
  private final ChangeRecordRepository changeRecordRepository;
  private final ScoreRepository scoreRepository;
  private final RewardRepository rewardRepository;
  private final GraduationRepository graduationRepository;

  public SummaryController(
      StudentRepository studentRepository,
      ChangeRecordRepository changeRecordRepository,
      ScoreRepository scoreRepository,
      RewardRepository rewardRepository,
      GraduationRepository graduationRepository) {
    this.studentRepository = studentRepository;
    this.changeRecordRepository = changeRecordRepository;
    this.scoreRepository = scoreRepository;
    this.rewardRepository = rewardRepository;
    this.graduationRepository = graduationRepository;
  }

  @GetMapping("/api/summary")
  public ApiResponse<SummaryPayload> summary(@AuthenticationPrincipal AppUser user) {
    List<StudentPayload> students;
    List<ChangePayload> changes;
    List<ScorePayload> scores;
    List<RewardPayload> rewards;
    List<GraduationPayload> graduation;
    if (user.isAdmin()) {
      students = studentRepository.findAllByOrderByIdAsc().stream().map(StudentPayload::from).toList();
      changes =
          changeRecordRepository.findAllByOrderByIdDesc().stream().map(ChangePayload::from).toList();
      scores = scoreRepository.findAllByOrderByIdAsc().stream().map(ScorePayload::from).toList();
      rewards = rewardRepository.findAllByOrderByIdAsc().stream().map(RewardPayload::from).toList();
      graduation =
          graduationRepository.findAllByOrderByIdAsc().stream()
              .map(GraduationPayload::from)
              .toList();
    } else {
      String sid = user.studentId();
      students = List.of();
      changes =
          changeRecordRepository.findByStudentIdOrderByIdDesc(sid).stream()
              .map(ChangePayload::from)
              .toList();
      scores =
          scoreRepository.findByStudentIdOrderByIdAsc(sid).stream()
              .map(ScorePayload::from)
              .toList();
      rewards =
          rewardRepository.findByStudentIdOrderByIdAsc(sid).stream()
              .map(RewardPayload::from)
              .toList();
      graduation = List.of();
    }
    return ApiResponse.ok(new SummaryPayload(students, changes, scores, rewards, graduation));
  }
}
