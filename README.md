# CampusEvent 校园活动报名与管理系统

《软件工程》实验二项目。系统已按 P2-F01—P2-F08 完成实现，默认使用 SQLite 单文件数据库，也支持切换 MySQL；前端生产构建产物已放入 Spring Boot，可用一个 Java 进程同时提供页面和 API。

## 当前状态

| 项 | 状态 |
|---|---|
| 需求范围 | P2-F01—P2-F08；Student / Admin 两种角色 |
| 后端 | Spring Boot 3.3.5 + Java 21 + MyBatis，默认 SQLite，可切 MySQL |
| 前端 | Vue 3 + Vite + Element Plus，学生端与管理员端共 9 个页面 |
| 数据库 | 默认 SQLite campusevent.db；可选 MySQL 8，库名 campusevent |
| 静态资源 | campusevent-web/dist 已复制到后端 src/main/resources/static |
| 首次启动 | 自动建表，并写入 8 个活动、36 个学生、116 条报名与通知 |
| 设计文档 | baseline-v1 设计已封存；change-v2 的 CR 标注、类图与时序图已同步，入口 [设计文档索引](docs/design/README.md) |
| 验收 | C1—C10 与 CR1/CR2 全部通过；自动化证据见 [回归测试表](docs/回归测试表.md) 与 [验收记录](docs/验收记录.md) |
| 自动化测试 | 24 项全部通过：C1—C10 与 CR1/CR2 验收 12 项、邮件失败 1 项、真实重启 1 项、策略单测 9 项、密码单测 1 项；前端生产构建通过 |
| 部署 | 已部署到 Vibe Hosting：https://cynyun-campus.thevibehosting.com/ |
| 基线 | baseline-v1（2026-11-23）保留；change-v2（2026-11-26，`fdd2364`）实现并部署 CR-01/CR-02，公网复测已通过 |

## 功能范围

| 编号 | 功能 | 说明 |
|---|---|---|
| P2-F01 | 活动 CRUD | 管理员创建、编辑、软删除活动；删除后历史报名与通知保留 |
| P2-F02 | 浏览与筛选 | 学生按名称、报名方式筛选活动，查看名额与状态 |
| P2-F03 | 报名 / 取消 | 学生报名、取消；同一活动只保留一条有效报名 |
| P2-F04 | 两种报名方式 | 直接报名立即通过；审核制报名进入待审核；直接报名可加年级限定（CR-01） |
| P2-F05 | 审批 | 管理员通过 / 拒绝；通过前复查容量 |
| P2-F06 | 四种状态 | PENDING、APPROVED、REJECTED、CANCELLED |
| P2-F07 | 站内通知与模拟邮件 | 报名提交、通过、拒绝、取消后写入站内消息；CR-02 追加模拟邮件日志 |
| P2-F08 | 活动统计 | 已通过人数、待审核数、剩余名额、使用率 |

明确不做：用户注册、找回密码、复杂权限、多人协作、真实邮件 / 短信服务、复杂日历、移动端 App、消息队列、分布式事务。邮件只做模拟发送并写入 email_log，不接 SMTP。登录保留 student / admin 两个固定演示账号。

## 技术栈

- 后端：Java 21、Spring Boot 3.3.5、Spring MVC、MyBatis 3.0.3、HikariCP、Bean Validation
- 数据库：默认 SQLite（org.xerial:sqlite-jdbc）；可选 MySQL 8
- 前端：Vue 3.5、Vite 8、Vue Router 5、Pinia 4、Element Plus 2.14、Axios
- 鉴权：HttpSession + 拦截器；固定演示账号，不引入 JWT / Spring Security
- 静态资源：Vue 构建产物由 Spring Boot classpath:/static/ 直接提供，Vue Router 使用 history 回退

## 架构与关键规则

~~~
浏览器（Vue 3 + Element Plus，static 由 Spring Boot 提供）
  → Spring MVC Controller（/api）
  → Service（事务边界）
  → RegistrationPolicy（DIRECT / APPROVAL / GRADE_LIMITED_DIRECT）
  → MyBatis Mapper
  → SQLite（默认）或 MySQL（可选）
      student / admin / activity / registration / message / email_log / app_meta
~~~

- 待审核不占名额；只有 APPROVED 计入已通过人数与剩余名额。
- 年级限定直接报名仅允许 grade 为 2 或 3 的学生；年级为空或越界返回 403，满员返回 409。
- 邮件通知由独立观察者写入 email_log；模拟失败只记 FAILED，不回滚报名。
- 同一学生同一活动只能有一条有效报名（PENDING / APPROVED）；已取消或已拒绝可重新报名。
- 审批通过前重新统计容量；满员时返回 409，报名保持 PENDING。
- MySQL 下报名 / 审批在事务内对活动行执行 SELECT ... FOR UPDATE；SQLite 不支持该语法，使用单连接池串行化写入。
- 活动删除为软删除（deleted_at）；学生端过滤，管理员可勾选“显示已删除”查看。
- 站内通知在状态变更后写入；通知失败不回滚主事务。
- 密码使用 SHA-256 存储；所有演示账号密码均为 123456。

## 目录

~~~
CampusEvent/
  README.md
  AI_DEV_LOG.md             AI 使用与过程记录
  scripts/
    build-all.ps1           构建前端并复制 dist 到 Spring Boot static
    build-all.cmd           Windows 双击入口
  docs/
    design/                 设计文档与 UML 图
    screenshots/            01—09 运行截图
    初始SRS分析.md          初始需求基线（P2-F01—F08、BR、C1—C10，冻结于 baseline-v1）
    变更影响分析.md         CR-01/CR-02 影响范围、兼容性与实施后记
    设计模式决策报告.md      策略/观察者选型、备选方案、代价与验证
    回归测试表.md            24 项测试与 C1—C10、CR1/CR2 对照
    验收记录.md             C1—C10 手工验收结果
  campusevent-server/       Spring Boot 后端
    src/main/java/com/Cynyun/campusevent/
      auth/                 登录、会话、角色拦截器
      controller/           学生端 / 管理员端接口、SPA 回退
      service/              活动、报名、消息业务
      policy/               直接报名 / 审核报名 / 年级限定策略与上下文
      event/                报名事件发布与站内通知观察者
      mail/                 模拟邮件观察者、发送器与邮件日志
      mapper/               MyBatis Mapper 接口（含邮件日志）
      config/               数据库方言、首次数据初始化、grade 迁移
      entity/ dto/ enums/   实体、请求体、枚举
    src/main/resources/
      application.yml       公共配置，默认 profile=sqlite
      application-sqlite.yml   SQLite 数据源与建表脚本
      application-mysql.yml    MySQL 数据源与建表脚本
      schema-sqlite.sql     SQLite 表结构
      schema-mysql.sql      MySQL 表结构
      static/               前端生产构建产物（由脚本生成）
      mapper/               MyBatis XML
    src/test/               C1—C10 与 CR 验收、真实重启、策略、邮件失败与密码测试（24 项）
  campusevent-web/          Vue 3 前端源码
    src/api/                 axios 接口封装
    src/views/               9 个页面
    src/components/          状态、报名方式标签
    src/layouts/             学生 / 管理员共用布局
    src/router/              路由与角色守卫
    src/stores/              登录状态
    src/styles/              全局样式
~~~

## 快速启动

### 1. 环境要求

- JDK 21
- Maven 3.9+
- Node.js 22.18+（仅重新构建前端时需要，当前开发环境 Node 26）
- MySQL 8.0（仅使用 mysql profile 时需要）

### 2. 默认方式：SQLite 单进程

仓库已经包含构建好的前端静态资源，可以直接启动后端：

~~~powershell
cd D:/SE/CampusEvent/campusevent-server
mvn spring-boot:run
~~~

或打包后运行：

~~~powershell
mvn -DskipTests package
java -jar target/campusevent-server.jar
~~~

打开 http://127.0.0.1:8080/ 。页面、静态资源和 API 都由 8080 提供，不需要再启动 Vite。

默认数据库文件为启动目录下的 campusevent.db。可用环境变量改路径：

~~~powershell
$env:CAMPUSEVENT_DB_PATH = "D:/SE/CampusEvent/campusevent.db"
~~~

首次启动会自动建表并写入演示数据；再次启动时通过 app_meta.demo_seeded 标记跳过，不会重复插入。

### 3. 重新构建前端并更新静态资源

修改 campusevent-web 后执行：

~~~powershell
cd D:/SE/CampusEvent
pwsh -File scripts/build-all.ps1
~~~

脚本等价于：

~~~powershell
cd campusevent-web
npm install
npm run build
# 将 dist 内容复制到 campusevent-server/src/main/resources/static
~~~

然后重新打包 / 重启后端。开发阶段仍可单独运行 Vite：

~~~powershell
cd D:/SE/CampusEvent/campusevent-web
npm run dev
~~~

Vite 已将 /api 代理到 http://localhost:8080 。

### 4. 可选方式：切换 MySQL

先创建数据库：

~~~sql
CREATE DATABASE IF NOT EXISTS campusevent
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
~~~

设置密码环境变量（默认读取 MySql80Key）：

~~~powershell
$env:MySql80Key = "你的 MySQL root 密码"
# 也可显式使用：
# $env:CAMPUSEVENT_MYSQL_PASSWORD = "你的密码"
~~~

用 mysql profile 启动：

~~~powershell
cd D:/SE/CampusEvent/campusevent-server
java -jar target/campusevent-server.jar --spring.profiles.active=mysql
# 或
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
~~~

MySQL 连接参数可用环境变量覆盖：

| 变量 | 默认值 |
|---|---|
| CAMPUSEVENT_MYSQL_URL | jdbc:mysql://localhost:3306/campusevent?... |
| CAMPUSEVENT_MYSQL_USER | root |
| CAMPUSEVENT_MYSQL_PASSWORD | MySql80Key 的值 |

从 MySQL 切回 SQLite：不设置 SPRING_PROFILES_ACTIVE 即可，默认 profile 为 sqlite。

### 5. 演示账号与初始化数据

| 角色 | 账号 | 密码 | 入口 |
|---|---|---|---|
| 学生 | student | 123456 | /activities |
| 管理员 | admin | 123456 | /admin/activities |
| 学生（批量） | student01—student36 | 123456 | /activities |
| 年级样例 | student（grade=2）、student01—student04（grade=1—4） | 123456 | /activities |

首次启动（数据库为空）时写入：

- 8 个未来活动：志愿服务、学术讲座、文体活动、创新创业等，直接报名与审核制混合；
- 36 个学生账号 student01—student36，姓名“演示学生01”—“演示学生36”，并补齐 grade 字段（student01—04 为 1—4，其余循环）；
- 116 条报名记录：部分 APPROVED、部分 PENDING，覆盖审核通过 / 待审核 / 容量占用等场景；
- 116 条站内通知，用于演示消息中心与未读数。

如需关闭批量演示数据但保留 student / admin 账号：

~~~powershell
$env:CAMPUSEVENT_SEED_DEMO = "false"
~~~

已有业务数据时（例如原 MySQL 库中已有活动），初始化器只写入标记，不会覆盖或重复插入。实现与重置方式见 [数据初始化说明](docs/数据初始化说明.md)。

## 页面与路由

| 端 | 路由 | 页面 |
|---|---|---|
| 公共 | /login | 登录 |
| 学生 | /activities | 活动广场：统计、筛选、活动卡片 |
| 学生 | /activities/:id | 活动详情：名额、报名规则、报名 / 取消 |
| 学生 | /my-registrations | 我的报名：状态与取消 |
| 学生 | /messages | 站内通知：未读、单条已读、全部已读 |
| 管理员 | /admin/activities | 活动管理：列表、筛选、软删除 |
| 管理员 | /admin/activities/new | 新建活动 |
| 管理员 | /admin/activities/:id/edit | 编辑活动 |
| 管理员 | /admin/registrations | 报名审核：按状态 / 活动筛选、通过、拒绝 |
| 管理员 | /admin/statistics | 数据统计：已通过、待审核、剩余名额、使用率 |

Spring Boot 的 SpaController 对上述前端路径回退到 index.html，刷新 /activities/1 等地址不会 404。

## 主要接口

| 方法 | 路径 | 角色 | 说明 |
|---|---|---|---|
| POST | /api/auth/login | 公共 | 登录，写入 Session |
| POST | /api/auth/logout | 登录 | 退出 |
| GET | /api/auth/me | 登录 | 当前用户 |
| GET | /api/activities | 登录 | 活动列表，支持 keyword / policyType / category |
| GET | /api/activities/{id} | 登录 | 活动详情 |
| POST | /api/student/activities/{id}/registrations | 学生 | 报名 |
| GET | /api/student/registrations | 学生 | 我的报名 |
| DELETE | /api/student/registrations/{id} | 学生 | 取消报名 |
| GET | /api/student/messages | 学生 | 站内消息 |
| POST | /api/student/messages/{id}/read | 学生 | 单条已读 |
| POST | /api/student/messages/read-all | 学生 | 全部已读 |
| GET | /api/student/messages/unread-count | 学生 | 未读数 |
| GET/POST/PUT/DELETE | /api/admin/activities... | 管理员 | 活动 CRUD |
| GET | /api/admin/statistics | 管理员 | 活动统计 |
| GET | /api/admin/registrations | 管理员 | 报名列表，支持 status / activityId |
| POST | /api/admin/registrations/{id}/approve | 管理员 | 通过 |
| POST | /api/admin/registrations/{id}/reject | 管理员 | 拒绝 |

统一响应格式：{ "code": 0, "message": "ok", "data": ... }；业务错误使用 400 / 401 / 403 / 404 / 409。

## 测试与验收

自动化测试（24 项）：

~~~powershell
cd D:/SE/CampusEvent/campusevent-server
mvn test
~~~

覆盖内容：CampusEventAcceptanceTest 12 项（C1—C10 + CR1 年级限定 + CR2 邮件日志幂等）；CampusEventRestartTest 真实重启 1 项；CampusEventEmailFailureTest 邮件失败 1 项；RegistrationPolicyTest 9 项；PasswordUtilTest 1 项。逐条说明见 [回归测试表](docs/回归测试表.md)。

前端生产构建：

~~~powershell
cd D:/SE/CampusEvent/campusevent-web
npm run build
~~~

C1—C10 与 CR1/CR2 的验收步骤、实际结果与证据见 [验收记录](docs/验收记录.md)；自动化方法名、断言与命令见 [回归测试表](docs/回归测试表.md)。C10 的“重启后数据保持”在 SQLite 默认模式下同样成立：数据库是启动目录下的单文件，重启只重新建表（IF NOT EXISTS），不会清空数据。

## 运行截图

| 截图 | 内容 |
|---|---|
| docs/screenshots/01-login.png | 登录页与学生 / 管理员演示账号 |
| docs/screenshots/02-activities.png | 学生活动广场：统计、筛选、名额 |
| docs/screenshots/03-activity-detail.png | 活动详情与报名 / 取消入口 |
| docs/screenshots/04-my-registrations.png | 我的报名与四种状态 |
| docs/screenshots/05-messages.png | 站内通知与未读标记 |
| docs/screenshots/06-admin-activities.png | 管理员活动列表 |
| docs/screenshots/07-admin-registrations.png | 报名审核与通过 / 拒绝 |
| docs/screenshots/08-admin-statistics.png | 活动统计与名额使用率 |
| docs/screenshots/09-activity-form.png | 新建活动表单 |

截图生成于 MySQL 演示数据版本，页面结构与当前版本一致；当前默认 SQLite 首次启动会生成更多活动与学生，功能验收不受影响。

## 设计文档与过程材料

- [初始 SRS 分析](docs/初始SRS分析.md)
- [设计模式决策报告](docs/设计模式决策报告.md)
- [变更影响分析](docs/变更影响分析.md)
- [回归测试表](docs/回归测试表.md)
- [AI 开发日志](AI_DEV_LOG.md)
- docs/design/README.md
- docs/design/01-use-case-description.md
- docs/design/02-class-diagram.md
- docs/design/03-sequence-direct-registration.md
- docs/design/04-sequence-review-registration.md
- docs/design/05-sequence-approval.md
- docs/design/00-design-review.md
- docs/design/diagrams/CampusEvent-design.drawio

## 公网部署

- 地址：https://cynyun-campus.thevibehosting.com/
- 当前线上版本：change-v2（`fdd2364`），含 CR-01 年级限定与 CR-02 模拟邮件；复测记录见 [docs/验收记录.md](docs/验收记录.md)
- 平台：Vibe Hosting，Ubuntu 24.04 + OpenJDK 21，单机 4 GB RAM 上限
- 进程：`/root/run.sh` 前台启动，JVM 堆 512 MB，Spring Boot 同时提供页面与 API
- 数据：服务器本地 SQLite，路径 `/root/myapp/data/campusevent.db`
- 休眠：保持 `never_sleep=false`，空闲后缩容，首次访问可能需要几秒唤醒
- 演示账号：`student / 123456`、`admin / 123456`；批量账号 `student01`—`student36`
- 部署脚本：[scripts/run-vibe.sh](scripts/run-vibe.sh)
- 公网登录页截图：[docs/screenshots/vibe-login.png](docs/screenshots/vibe-login.png)

### 公网登录 403 的 CORS 说明

浏览器从公网域名登录时，同源 POST 请求也会带上 `Origin: https://cynyun-campus.thevibehosting.com`。如果后端 CORS 只允许本地 Vite 地址，Spring 会在进入 Controller 前直接返回 403，表现为“服务器能启动、接口用命令行测试正常，但浏览器登录失败”。

本项目已在 `campusevent-server/src/main/java/com/Cynyun/campusevent/config/WebConfig.java` 中放行：

- `http://localhost:*`、`http://127.0.0.1:*`（本地开发）
- `https://cynyun-campus.thevibehosting.com`（当前公网域名）
- `https://*.thevibehosting.com`（Vibe Hosting 子域名）

更换公网域名或反向代理地址后，必须同步修改这里，否则登录、报名等 POST 接口会再次出现 403。验证方式：

~~~powershell
curl.exe -i -X POST "https://cynyun-campus.thevibehosting.com/api/auth/login" -H "Origin: https://cynyun-campus.thevibehosting.com" -H "Content-Type: application/json" -d '{"username":"admin","password":"123456"}'
~~~

预期返回 `HTTP/1.1 200` 且 `data.role` 为 `ADMIN`；若仍是 403，先检查响应头是否带 `Access-Control-Allow-Origin`，再核对 `WebConfig` 的允许列表。

## 已知限制

- 使用固定演示账号与会话登录，未实现注册、找回密码和细粒度权限。
- SQLite 模式为单连接池，适合课程演示与单机使用；高并发写入应切换 MySQL。
- 活动列表未分页；数据量大时需补充分页与索引优化。
- 站内通知为同步写入，未引入消息队列；通知失败不会重试，也没有幂等键。重试被状态机挡住，但 afterCommit 崩溃窗口仍可能丢通知（U2）。
- 模拟邮件同样在报名事务提交后发送；失败只写 email_log FAILED，不重试、不回滚报名，真实 SMTP 与退信处理未实现。
- 活动软删除只支持删除与查看已删除，未实现“恢复活动”端点。
- C1—C10 与 CR1/CR2 只在 SQLite 上跑自动化；MySQL 仅做启动与基本接口验证，未做 MySQL 全量回归与高并发压测。
- 前端没有 Playwright/Cypress 自动化，页面验收依赖手工冒烟与截图。
- 公网环境使用单机 SQLite 与按需休眠，未实现生产级备份、监控、限流与多实例容灾。
- 示例密码 123456 仅用于课程演示，公网地址已公开，不应存放真实用户数据。
