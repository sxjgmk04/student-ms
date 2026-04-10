# 学生学籍管理系统（student-ms）

本项目为学籍管理系统示例：前端基于 Vue 3 + Vite，后端基于 Spring Boot + MySQL。

首次运行建议优先使用 Docker 方案，环境一致性更高、部署步骤更少。

## 先说最快的：Docker 跑起来

### 你需要准备

- 装好 Docker Desktop
- Docker 已经启动
- 下载地址（官方）：[https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/)

### 为什么建议用 Docker

- 无需分别安装 MySQL / JDK / Maven / Node，本地依赖更少
- 可显著降低因版本差异导致的运行问题
- 一条命令即可同时启动前端、后端与数据库

### 一条命令启动

在项目根目录执行：

```powershell
docker compose up -d --build
```

启动后访问：

- 页面：`http://localhost:8080`
- 后端：`http://localhost:3000`（通常无需直接访问，前端通过 `/api` 反向代理）

默认演示账号：

- `admin / 123456`
- `student1` 到 `student8` / `123456`

停止：

```powershell
docker compose down
```

如需同时清理数据库卷（恢复到首次启动状态）：

```powershell
docker compose down -v
```

---

## 不用 Docker 的本地启动（开发用）

### 需要的软件

- Node.js 20+
- JDK 17+
- Maven 3.9+
- MySQL 8.0+

### 第一步：建库

```sql
CREATE DATABASE student_ms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 第二步：启动后端

```powershell
cd c:\Users\Admin\student-ms\server-spring
.\run-dev.ps1 -MysqlPassword 123456
```

后端默认 `http://localhost:3000`。

### 第三步：启动前端

```powershell
cd c:\Users\Admin\student-ms
npm install
npm run dev
```

前端默认 `http://localhost:5173`。

开发环境下，前端 `/api` 会自动代理到后端 `3000` 端口。

### 懒人本地一键启动（非 Docker）

```powershell
cd c:\Users\Admin\student-ms
.\start-dev.ps1 -MysqlPassword 123456
```

脚本会自动打开两个 PowerShell 窗口，分别启动后端与前端。

---

## 脱敏演示数据导入

项目内置了可重复导入的**脱敏演示数据**流程（虚构样本，非真实个人信息），用于论文展示和系统功能演示。

在项目根目录执行：

```powershell
cd c:\Users\Admin\student-ms
npm run import:thesis-demo
mysql -u root -p --default-character-set=utf8mb4 student_ms -e "source c:/Users/Admin/student-ms/server-spring/sql/thesis_desensitized_seed.sql"
```

说明：

- 会先清理历史导入批次（如 `202401*` 批次及旧 UCI 批次），再写入新的脱敏样本
- 可重复执行，适合演示前快速重置数据
- 导入后可使用内置账号进行展示（如 `admin / 123456`）

更详细的数据范围和字段说明见 `server-spring/README.md` 的对应章节。

---

## 常见问题

- 端口冲突：`5173`（前端）、`3000`（后端）、`8080`（Docker 前端）、`3306`（MySQL）
- MySQL 连不上：确认 MySQL 已启动、密码正确、数据库 `student_ms` 已创建
- PowerShell 不让跑脚本：执行 `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned`
