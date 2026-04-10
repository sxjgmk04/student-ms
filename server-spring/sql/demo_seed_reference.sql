
SET NAMES utf8mb4;

INSERT INTO users (username, password_hash, role, name, student_id) VALUES
('admin', '$2a$10$AJR8E3bXWqF3XtpdaHrPPOPug5p/lrGFwv3SrCmMZ2ftns0aByFgG', 'admin', '张管理员', NULL),
('student1', '$2a$10$AJR8E3bXWqF3XtpdaHrPPOPug5p/lrGFwv3SrCmMZ2ftns0aByFgG', 'student', '李同学', '2021001'),
('student2', '$2a$10$AJR8E3bXWqF3XtpdaHrPPOPug5p/lrGFwv3SrCmMZ2ftns0aByFgG', 'student', '王同学', '2021002'),
('student3', '$2a$10$AJR8E3bXWqF3XtpdaHrPPOPug5p/lrGFwv3SrCmMZ2ftns0aByFgG', 'student', '赵敏', '2021003'),
('student4', '$2a$10$AJR8E3bXWqF3XtpdaHrPPOPug5p/lrGFwv3SrCmMZ2ftns0aByFgG', 'student', '陈浩', '2021004'),
('student5', '$2a$10$AJR8E3bXWqF3XtpdaHrPPOPug5p/lrGFwv3SrCmMZ2ftns0aByFgG', 'student', '孙悦', '2021005'),
('student6', '$2a$10$AJR8E3bXWqF3XtpdaHrPPOPug5p/lrGFwv3SrCmMZ2ftns0aByFgG', 'student', '周杰', '2021006'),
('student7', '$2a$10$AJR8E3bXWqF3XtpdaHrPPOPug5p/lrGFwv3SrCmMZ2ftns0aByFgG', 'student', '吴婷', '2021007'),
('student8', '$2a$10$AJR8E3bXWqF3XtpdaHrPPOPug5p/lrGFwv3SrCmMZ2ftns0aByFgG', 'student', '马强', '2021008');

INSERT INTO students (student_id, name, gender, major, class_name, grade, status) VALUES
('2021001', '李同学', '男', '计算机科学与技术', '计科2101', 2021, '在读'),
('2021002', '王同学', '女', '软件工程', '软工2101', 2021, '在读'),
('2021003', '赵敏', '女', '信息管理与信息系统', '信管2101', 2021, '在读'),
('2021004', '陈浩', '男', '网络工程', '网工2101', 2021, '在读'),
('2021005', '孙悦', '女', '数据科学与大数据技术', '数据2101', 2021, '在读'),
('2021006', '周杰', '男', '智能科学与技术', '智科2101', 2021, '在读'),
('2021007', '吴婷', '女', '信息安全', '信安2101', 2021, '在读'),
('2021008', '马强', '男', '物联网工程', '物联2101', 2021, '在读');

INSERT INTO changes (student_id, name, type, reason, status, date) VALUES
('2021001', '李同学', '转专业', '兴趣方向调整', '审核中', '2024-03-01'),
('2021002', '王同学', '休学', '因病休养一学期', '审核中', '2024-04-12');

INSERT INTO scores (student_id, name, course, score, semester) VALUES
('2021001', '李同学', '高等数学', 85, '2023-2024-1'),
('2021001', '李同学', '大学英语', 90, '2023-2024-1'),
('2021001', '李同学', '数据结构', 88, '2023-2024-2'),
('2021002', '王同学', '高等数学', 78, '2023-2024-1'),
('2021002', '王同学', '程序设计基础', 92, '2023-2024-1'),
('2021003', '赵敏', '管理学原理', 82, '2023-2024-1'),
('2021004', '陈浩', '计算机网络', 79, '2023-2024-2'),
('2021005', '孙悦', '概率统计', 91, '2023-2024-1'),
('2021006', '周杰', '机器学习导论', 86, '2023-2024-2'),
('2021007', '吴婷', '密码学基础', 84, '2023-2024-2'),
('2021008', '马强', '嵌入式系统', 80, '2023-2024-2');

INSERT INTO rewards (student_id, name, type, title, date) VALUES
('2021001', '李同学', '奖励', '校级一等奖学金', '2023-12-01'),
('2021002', '王同学', '奖励', 'ACM校赛铜奖', '2024-01-15'),
('2021005', '孙悦', '惩罚', '旷课通报批评', '2024-02-20');

INSERT INTO graduation (student_id, name, major, status, thesis_score) VALUES
('2021001', '李同学', '计算机科学与技术', '待审核', 85),
('2021002', '王同学', '软件工程', '待审核', 81),
('2021006', '周杰', '智能科学与技术', '待审核', 88);
