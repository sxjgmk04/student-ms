# student-ms 后端服务说明（Spring Boot）

这是我在 `student-ms` 项目中的后端模块，技术栈为 Spring Boot + Spring Security + JPA + MySQL。
本文档以“可复现部署”为目标，覆盖环境准备、启动流程、演示数据导入、接口约定和安全配置建议。

## 运行环境

- JDK 17+
- Maven 3.9+
- MySQL 8.0+

## 1. 数据库初始化

在 MySQL 客户端执行：

```sql
CREATE DATABASE student_ms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 2. 启动后端服务

### 方式 A（手动设置环境变量）

```powershell
cd c:\Users\Admin\student-ms\server-spring
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="your_mysql_password"
# 建议 32 字节以上随机串；不足 32 字节时程序会做 SHA-256 派生
$env:JWT_SECRET="please-use-a-long-random-secret-key-ok"
# 可选：逗号分隔多个前端源
# $env:CORS_ALLOWED_ORIGINS="https://your-frontend-domain"
mvn spring-boot:run
```

### 方式 B（推荐，开发便捷）

在 `server-spring` 目录执行：

```powershell
.\run-dev.ps1
```

如果 MySQL 密码不是默认值 `root`：

```powershell
.\run-dev.ps1 -MysqlPassword 123456
```

后端默认地址：`http://localhost:3000`

## 3. 启动前端（联调）

另开终端执行：

```powershell
cd c:\Users\Admin\student-ms
npm run dev
```

前端默认地址：`http://localhost:5173`

## 内置演示账号

首次启动且 `users` 表为空时，系统会自动写入参考数据（`sql/demo_seed_reference.sql`）中的虚构账号（非真实个人信息）：

- `admin / 123456`
- `student1` ... `student8` / `123456`（对应学号 `2021001` ... `2021008`）

## 论文演示用脱敏数据（推荐）

我在项目中提供了可重复导入的高校样本数据（全虚构、脱敏），便于答辩和功能复现。
导入流程会先清理历史 UCI 批次（`UCI-MAT-*` / `uci_mat_*`）和 `202401*` 批次，再写入新样本。

```powershell
cd c:\Users\Admin\student-ms
npm run import:thesis-demo
mysql -u root -p --default-character-set=utf8mb4 student_ms -e "source c:/Users/Admin/student-ms/server-spring/sql/thesis_desensitized_seed.sql"
```

样本特征（摘要）：

- 学号范围：`202401001` ... `202401280`（约 280 人，18 个专业）
- 登录名范围：`stu202401001` ... `stu202401280`，默认密码 `123456`
- 成绩数据：两学期、约 13 门课程，按专业维度生成，分布近似正态
- `data_source` 标注为“毕业论文演示（虚构脱敏）”

可选：早期实验使用的 UCI 中学数据导入脚本：

- `npm run import:uci-sql`
- 说明文档：`data/open/README.md`

## 手工导入参考数据（可选）

若需直接用 SQL 灌入参考数据，确保表结构已由应用创建（`ddl-auto: update`）或已手工建表，并保证 `users` 表为空，避免唯一键冲突：

- `server-spring/sql/demo_seed_reference.sql`

## 业务规则补充：学籍异动

- 管理员：`POST /api/changes` 可为任意学生录入异动申请
- 学生：同一接口在 `student` 角色下仅允许提交本人申请；后端以 JWT 中身份字段为准，忽略请求体内的学号/姓名篡改，状态固定为“审核中”

## 健康检查

- `GET /api/health`（无需认证）
- 返回示例：`{ "code": 0, "data": { "status": "UP" } }`

## API 路由约定

- `/api/health`
- `/api/auth/login`、`/api/auth/logout`、`/api/auth/me`
- `/api/students`
- `/api/students/me`（学生本人档案）
- `/api/changes`
- `/api/scores`
- `/api/rewards`
- `/api/graduation`
- `/api/audit-logs`
- `/api/summary`
- `/api/analytics/insights`（管理员视角：规则引擎 + 逻辑回归式风险概率 + 专业聚合）
- `/api/analytics/my-insight`（学生视角：个人学情摘要与风险提示）

统一响应结构：

- 成功：`{ "code": 0, "data": ... }`
- 失败：`{ "code": <http_status>, "message": "..." }`

## 生产环境安全配置建议

| 变量 / 配置 | 说明 |
|-------------|------|
| `JWT_SECRET` | 使用高强度随机串。若直接作为 HMAC 密钥，建议至少 32 字节（UTF-8）。 |
| `JWT_EXPIRATION_MS` | 建议按业务收敛有效期，降低 Token 泄露风险窗口。 |
| `CORS_ALLOWED_ORIGINS` | 仅允许已上线前端域名，多个来源用逗号分隔。 |
| `MYSQL_*` | 使用最小权限数据库账号，避免生产环境使用 `root`。 |
| `JPA_DDL_AUTO` | 建议设为 `validate` 或 `none`，避免运行期自动改表。 |
| HTTPS | 全站 TLS，避免明文传输认证信息。 |

前端侧建议：生产构建使用 `VITE_API_BASE` 指向 HTTPS API，避免引入不可信第三方脚本。

## 常见问题排查

- 3000 端口冲突：终止占用进程或更换 `PORT`
- MySQL 连接失败：检查 `MYSQL_USER` / `MYSQL_PASSWORD` / `MYSQL_HOST` / `MYSQL_DB`
- JWT 密钥长度不足：更换更长随机串
