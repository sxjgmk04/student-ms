package com.studentms.bootstrap;

import com.studentms.entity.AnnouncementEntity;
import com.studentms.repo.AnnouncementRepository;
import java.time.Instant;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class AnnouncementBootstrap implements ApplicationRunner {

  private final AnnouncementRepository announcementRepository;

  public AnnouncementBootstrap(AnnouncementRepository announcementRepository) {
    this.announcementRepository = announcementRepository;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (announcementRepository.count() > 0) {
      return;
    }
    announcementRepository.save(
        ann(
            "关于 2024 春季学期学籍核对的通知",
            "请全体在籍学生于本周内登录系统核对个人信息，如有误请联系教务处。",
            Instant.now().minusSeconds(86400)));
    announcementRepository.save(
        ann(
            "学籍异动申请流程说明",
            "学生提交异动申请后，由学院教务员初审，教务处终审。审核结果将在「学籍异动」模块更新。",
            Instant.now().minusSeconds(3600 * 48)));
    announcementRepository.save(
        ann(
            "系统维护窗口",
            "每周日 02:00–04:00 可能进行例行维护，期间或出现短暂无法访问，敬请谅解。",
            Instant.now().minusSeconds(3600 * 12)));
  }

  private static AnnouncementEntity ann(String title, String content, Instant at) {
    AnnouncementEntity e = new AnnouncementEntity();
    e.setTitle(title);
    e.setContent(content);
    e.setCreatedAt(at);
    return e;
  }
}
