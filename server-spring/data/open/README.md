# 开源数据集（UCI）

本目录包含 **UCI Machine Learning Repository** 的 **Student Performance** 数据集，用于向本系统导入真实结构的开源学业数据（非本系统虚构演示数据）。

- **官方页面**：https://archive.ics.uci.edu/ml/datasets/Student+Performance  
- **数据文件**：`uci_extract/student-mat.csv`（葡萄牙某中学**数学**课成绩，分号分隔）  
- **引用**（学术/作业请保留）：

  > P. Cortez and A. Silva. Using Data Mining to Predict Secondary School Student Performance. In A. Brito and F. Teixeira Eds., Proceedings of 5th FUture BUsiness TEChnology Conference (FUBUTEC 2008) pp. 5-12, Porto, Portugal, April, 2008, EUROSIS, ISBN 978-9077381-39-7.

- **使用说明**：见仓库根目录与 `server-spring/README.md` 中「UCI 开源数据导入」一节。导入脚本由 `scripts/generate-uci-sql.mjs` 根据 CSV **重新生成**（学号前缀 `UCI-MAT-*`，姓名为匿名「学生0001」形式）。

**隐私说明**：原始数据为教育研究用匿名记录；本系统展示时仅保留课程统计字段映射后的学业信息，登录账号为系统生成的 `uci_mat_*`，密码默认与演示环境一致（见导入说明）。
