package com.studentms.bootstrap;

import com.studentms.entity.ChangeRecordEntity;
import com.studentms.entity.GraduationEntity;
import com.studentms.entity.RewardEntity;
import com.studentms.entity.ScoreEntity;
import com.studentms.entity.StudentEntity;
import com.studentms.entity.UserEntity;
import com.studentms.repo.ChangeRecordRepository;
import com.studentms.repo.GraduationRepository;
import com.studentms.repo.RewardRepository;
import com.studentms.repo.ScoreRepository;
import com.studentms.repo.StudentRepository;
import com.studentms.repo.UserRepository;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

  private record SeedStudent(
      String username,
      String studentId,
      String name,
      String gender,
      String major,
      String className,
      int grade) {}

  private static final List<SeedStudent> EXTRA_STUDENTS =
      List.of(
          new SeedStudent(
              "student3", "2021003", "赵敏", "女", "信息管理与信息系统", "信管2101", 2021),
          new SeedStudent("student4", "2021004", "陈浩", "男", "网络工程", "网工2101", 2021),
          new SeedStudent(
              "student5", "2021005", "孙悦", "女", "数据科学与大数据技术", "数据2101", 2021),
          new SeedStudent("student6", "2021006", "周杰", "男", "智能科学与技术", "智科2101", 2021),
          new SeedStudent("student7", "2021007", "吴婷", "女", "信息安全", "信安2101", 2021),
          new SeedStudent("student8", "2021008", "马强", "男", "物联网工程", "物联2101", 2021));

  private final UserRepository userRepository;
  private final StudentRepository studentRepository;
  private final ChangeRecordRepository changeRecordRepository;
  private final ScoreRepository scoreRepository;
  private final RewardRepository rewardRepository;
  private final GraduationRepository graduationRepository;
  private final PasswordEncoder passwordEncoder;

  public DataInitializer(
      UserRepository userRepository,
      StudentRepository studentRepository,
      ChangeRecordRepository changeRecordRepository,
      ScoreRepository scoreRepository,
      RewardRepository rewardRepository,
      GraduationRepository graduationRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.studentRepository = studentRepository;
    this.changeRecordRepository = changeRecordRepository;
    this.scoreRepository = scoreRepository;
    this.rewardRepository = rewardRepository;
    this.graduationRepository = graduationRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (userRepository.count() > 0) return;

    String hash = passwordEncoder.encode("123456");

    UserEntity admin = new UserEntity();
    admin.setUsername("admin");
    admin.setPasswordHash(hash);
    admin.setRole("admin");
    admin.setName("张管理员");
    admin.setStudentId(null);
    userRepository.save(admin);

    UserEntity s1 = new UserEntity();
    s1.setUsername("student1");
    s1.setPasswordHash(hash);
    s1.setRole("student");
    s1.setName("李同学");
    s1.setStudentId("2021001");
    userRepository.save(s1);

    UserEntity s2 = new UserEntity();
    s2.setUsername("student2");
    s2.setPasswordHash(hash);
    s2.setRole("student");
    s2.setName("王同学");
    s2.setStudentId("2021002");
    userRepository.save(s2);

    for (SeedStudent seed : EXTRA_STUDENTS) {
      UserEntity u = new UserEntity();
      u.setUsername(seed.username());
      u.setPasswordHash(hash);
      u.setRole("student");
      u.setName(seed.name());
      u.setStudentId(seed.studentId());
      userRepository.save(u);
    }

    StudentEntity st1 = new StudentEntity();
    st1.setStudentId("2021001");
    st1.setName("李同学");
    st1.setGender("男");
    st1.setMajor("计算机科学与技术");
    st1.setClassName("计科2101");
    st1.setGrade(2021);
    st1.setStatus("在读");
    studentRepository.save(st1);

    StudentEntity st2 = new StudentEntity();
    st2.setStudentId("2021002");
    st2.setName("王同学");
    st2.setGender("女");
    st2.setMajor("软件工程");
    st2.setClassName("软工2101");
    st2.setGrade(2021);
    st2.setStatus("在读");
    studentRepository.save(st2);

    for (SeedStudent seed : EXTRA_STUDENTS) {
      StudentEntity st = new StudentEntity();
      st.setStudentId(seed.studentId());
      st.setName(seed.name());
      st.setGender(seed.gender());
      st.setMajor(seed.major());
      st.setClassName(seed.className());
      st.setGrade(seed.grade());
      st.setStatus("在读");
      studentRepository.save(st);
    }

    ChangeRecordEntity ch = new ChangeRecordEntity();
    ch.setStudentId("2021001");
    ch.setName("李同学");
    ch.setType("转专业");
    ch.setReason("兴趣方向调整");
    ch.setStatus("审核中");
    ch.setDate("2024-03-01");
    changeRecordRepository.save(ch);

    ChangeRecordEntity ch2 = new ChangeRecordEntity();
    ch2.setStudentId("2021002");
    ch2.setName("王同学");
    ch2.setType("休学");
    ch2.setReason("因病休养一学期");
    ch2.setStatus("审核中");
    ch2.setDate("2024-04-12");
    changeRecordRepository.save(ch2);

    scoreRepository.save(score("2021001", "李同学", "高等数学", 85, "2023-2024-1"));
    scoreRepository.save(score("2021001", "李同学", "大学英语", 90, "2023-2024-1"));
    scoreRepository.save(score("2021001", "李同学", "数据结构", 88, "2023-2024-2"));
    scoreRepository.save(score("2021002", "王同学", "高等数学", 78, "2023-2024-1"));
    scoreRepository.save(score("2021002", "王同学", "程序设计基础", 92, "2023-2024-1"));
    scoreRepository.save(score("2021003", "赵敏", "管理学原理", 82, "2023-2024-1"));
    scoreRepository.save(score("2021004", "陈浩", "计算机网络", 79, "2023-2024-2"));
    scoreRepository.save(score("2021005", "孙悦", "概率统计", 91, "2023-2024-1"));
    scoreRepository.save(score("2021006", "周杰", "机器学习导论", 86, "2023-2024-2"));
    scoreRepository.save(score("2021007", "吴婷", "密码学基础", 84, "2023-2024-2"));
    scoreRepository.save(score("2021008", "马强", "嵌入式系统", 80, "2023-2024-2"));

    RewardEntity rw = new RewardEntity();
    rw.setStudentId("2021001");
    rw.setName("李同学");
    rw.setType("奖励");
    rw.setTitle("校级一等奖学金");
    rw.setDate("2023-12-01");
    rewardRepository.save(rw);

    RewardEntity rw2 = new RewardEntity();
    rw2.setStudentId("2021002");
    rw2.setName("王同学");
    rw2.setType("奖励");
    rw2.setTitle("ACM校赛铜奖");
    rw2.setDate("2024-01-15");
    rewardRepository.save(rw2);

    RewardEntity rw3 = new RewardEntity();
    rw3.setStudentId("2021005");
    rw3.setName("孙悦");
    rw3.setType("惩罚");
    rw3.setTitle("旷课通报批评");
    rw3.setDate("2024-02-20");
    rewardRepository.save(rw3);

    GraduationEntity g = new GraduationEntity();
    g.setStudentId("2021001");
    g.setName("李同学");
    g.setMajor("计算机科学与技术");
    g.setStatus("待审核");
    g.setThesisScore(85);
    graduationRepository.save(g);

    GraduationEntity g2 = new GraduationEntity();
    g2.setStudentId("2021002");
    g2.setName("王同学");
    g2.setMajor("软件工程");
    g2.setStatus("待审核");
    g2.setThesisScore(81);
    graduationRepository.save(g2);

    GraduationEntity g3 = new GraduationEntity();
    g3.setStudentId("2021006");
    g3.setName("周杰");
    g3.setMajor("智能科学与技术");
    g3.setStatus("待审核");
    g3.setThesisScore(88);
    graduationRepository.save(g3);
  }

  private static ScoreEntity score(
      String studentId, String name, String course, int score, String semester) {
    ScoreEntity sc = new ScoreEntity();
    sc.setStudentId(studentId);
    sc.setName(name);
    sc.setCourse(course);
    sc.setScore(score);
    sc.setSemester(semester);
    return sc;
  }
}
