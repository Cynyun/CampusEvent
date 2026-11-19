# CampusEvent 校园活动报名与管理系统

《软件工程》实验二项目。系统已按 P2-F01—P2-F08 完成实现，可本地运行、可演示、可按 C1—C10 逐条验收。

## 当前状态

| 项 | 状态 |
|---|---|
| 需求范围 | P2-F01—P2-F08；Student / Admin 两种角色 |
| 后端 | Spring Boot 3.3.5 + Java 21 + MyBatis + MySQL，已实现并启动验证 |
| 前端 | Vue 3 + Vite + Element Plus，学生端与管理员端共 9 个页面 |
| 数据库 | MySQL 8，库名 campusevent；schema.sql / data.sql 随应用启动执行 |
| 设计文档 | 已完成，入口 [docs/design/README.md](docs/design/README.md) |
| 验收 | C1—C10 全部通过，记录见 [docs/验收记录.md](docs/验收记录.md) |
| 自动化测试 | 后端单元测试通过；前端生产构建通过 |
| 部署 | 未部署公网；本地开发环境运行 |

## 功能范围

| 编号 | 功能 | 说明 |
|---|---|---|
| P2-F01 | 活动 CRUD | 管理员创建、编辑、软删除活动；删除后历史报名与通知保留 |
| P2-F02 | 浏览与筛选 | 学生按名称、报名方式筛选活动，查看名额与状态 |
| P2-F03 | 报名 / 取消 | 学生报名、取消；同一活动只保留一条有效报名 |
| P2-F04 | 两种报名方式 | 直接报名立即通过；审核制报名进入待审核 |
| P2-F05 | 审批 | 管理员通过 / 拒绝；通过前复查容量 |
| P2-F06 | 四种状态 | PENDING、APPROVED、REJECTED、CANCELLED |
| P2-F07 | 站内通知 | 报名提交、通过、拒绝、取消后写入站内消息 |
| P2-F08 | 活动统计 | 已通过人数、待审核数、剩余名额、使用率 |

明确不做：用户注册、找回密码、复杂权限、多人协作、邮件 / 短信、复杂日历、移动端 App、消息队列、分布式事务。登录仅保留 student / admin 两个固定演示账号。

## 技术栈

- 后端：Java 21、Spring Boot 3.3.5、Spring MVC、MyBatis 3.0.3、MySQL 8、HikariCP、Bean Validation
- 前端：Vue 3.5、Vite 8、Vue Router 5、Pinia 4、Element Plus 2.14、Axios
- 鉴权：HttpSession + 拦截器；固定演示账号，不引入 JWT / Spring Security
- 数据库：MySQL，utf8mb4；单库 campusevent

## 架构与关键规则

```
浏览器（Vue 3 + Element Plus）
  → Vite dev server /api 代理
  → Spring MVC Controller
  → Service（事务边界）
  → RegistrationPolicy（DIRECT / APPROVAL）
  → MyBatis Mapper
  → MySQL（student / admin / activity / registration / message）
```

- 待审核不占名额；只有 APPROVED 计入已通过人数与剩余名额。
- 同一学生同一活动只能有一条有效报名（PENDING / APPROVED）；已取消或已拒绝可重新报名。
- 审批通过前重新统计容量；满员时返回 409，报名保持 PENDING。
- 报名 / 审批在事务内对活动行执行 `SELECT ... FOR UPDATE`，避免并发超卖。
- 活动删除为软删除（deleted_at）；学生端过滤，管理员可勾选“显示已删除”查看。
- 站内通知在状态变更后写入；通知失败不回滚主事务。
- 密码使用 SHA-256 存储；演示账号密码均为 123456。

## 目录

```
CampusEvent/
  README.md
  docs/
    design/                 设计文档与 UML 图
    screenshots/            01—09 运行截图
    验收记录.md             C1—C10 验收结果
  campusevent-server/       Spring Boot 后端
    src/main/java/com/Cynyun/campusevent/
      auth/                 登录、会话、角色拦截器
      controller/           学生端 / 管理员端接口
      service/              活动、报名、消息业务
      policy/               直接报名 / 审核报名策略
      event/                报名事件与站内通知观察者
      mapper/               MyBatis Mapper 接口
      entity/ dto/ enums/   实体、请求体、枚举
    src/main/resources/
      schema.sql            建表脚本
      data.sql              演示账号初始化
      mapper/               MyBatis XML
      application.yml       端口、数据源、MyBatis 配置
    src/test/               策略与密码单元测试
  campusevent-web/          Vue 3 前端
    src/api/                 axios 接口封装
    src/views/               9 个页面
    src/components/          状态、报名方式标签
    src/layouts/             学生 / 管理员共用布局
    src/router/              路由与角色守卫
    src/stores/              登录状态
    src/styles/              全局样式
```

## 快速启动

### 1. 环境要求

- JDK 21
- Maven 3.9+
- Node.js 22.18+（当前开发环境 Node 26）
- MySQL 8.0，服务已启动

### 2. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS campusevent
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
```

表结构与演示账号由应用启动时自动执行 `schema.sql` 和 `data.sql` 创建；重复启动不会重复插入账号。

### 3. 配置数据库密码

后端从环境变量 `MYSQL80KEY` 读取 MySQL root 密码：

```powershell
$env:MYSQL80KEY = "你的 MySQL root 密码"
```

持久化到用户环境变量（可选）：

```powershell
[Environment]::SetEnvironmentVariable("MYSQL80KEY", "你的密码", "User")
```

不要把自己的密码写进 `application.yml` 后提交。

### 4. 启动后端

```powershell
cd D:\SE\CampusEvent\campusevent-server
mvn spring-boot:run
```

或在 IDEA 中运行 `com.Cynyun.campusevent.CampusEventApplication`，并在 Run Configuration 中设置 `MYSQL80KEY`。

后端地址：http://localhost:8080

### 5. 启动前端

```powershell
cd D:\SE\CampusEvent\campusevent-web
npm install
npm run dev
```

前端地址：http://127.0.0.1:5173

Vite 已将 `/api` 代理到 `http://localhost:8080`，无需额外配置跨域。

### 6. 演示账号

| 角色 | 账号 | 密码 | 入口 |
|---|---|---|---|
| 学生 | student | 123456 | /activities |
| 管理员 | admin | 123456 | /admin/activities |

演示数据（4 个活动与报名记录）存放在本地 MySQL 中，不随仓库提交；新环境可用管理员账号创建活动后演示。

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

统一响应格式：`{ "code": 0, "message": "ok", "data": ... }`；业务错误使用 400 / 401 / 403 / 404 / 409。

## 测试与验收

后端单元测试：

```powershell
cd D:\SE\CampusEvent\campusevent-server
mvn test
```

前端生产构建：

```powershell
cd D:\SE\CampusEvent\campusevent-web
npm run build
```

C1—C10 的验收步骤、实际结果与证据见 [docs/验收记录.md](docs/验收记录.md)。其中 C10 已通过“停止后端 → 重新启动 → 重新登录”验证：4 个活动、4 条报名记录、1 条未读消息均保持一致。

## 运行截图

| 截图 | 内容 |
|---|---|
| [01-login.png](docs/screenshots/01-login.png) | 登录页与学生 / 管理员演示账号 |
| [02-activities.png](docs/screenshots/02-activities.png) | 学生活动广场：统计、筛选、名额 |
| [03-activity-detail.png](docs/screenshots/03-activity-detail.png) | 活动详情与报名 / 取消入口 |
| [04-my-registrations.png](docs/screenshots/04-my-registrations.png) | 我的报名与四种状态 |
| [05-messages.png](docs/screenshots/05-messages.png) | 站内通知与未读标记 |
| [06-admin-activities.png](docs/screenshots/06-admin-activities.png) | 管理员活动列表 |
| [07-admin-registrations.png](docs/screenshots/07-admin-registrations.png) | 报名审核与通过 / 拒绝 |
| [08-admin-statistics.png](docs/screenshots/08-admin-statistics.png) | 活动统计与名额使用率 |
| [09-activity-form.png](docs/screenshots/09-activity-form.png) | 新建活动表单 |

## 设计文档

- [设计文档索引](docs/design/README.md)
- [用例说明](docs/design/01-use-case-description.md)
- [UML 类图](docs/design/02-class-diagram.md)
- [直接报名时序](docs/design/03-sequence-direct-registration.md)
- [审核型报名时序](docs/design/04-sequence-review-registration.md)
- [审批时序](docs/design/05-sequence-approval.md)
- [设计评审表](docs/design/00-design-review.md)
- [draw.io 源文件](docs/design/diagrams/CampusEvent-design.drawio)

## 已知限制

- 使用固定演示账号与会话登录，未实现注册、找回密码和细粒度权限。
- 活动列表未分页；数据量大时需补充分页与索引优化。
- 站内通知为同步写入，未引入消息队列；通知失败不会重试。
- 未部署公网，未实现 HTTPS、生产级备份、监控与限流。
- 演示数据仅存在本地 MySQL；换环境后需重新创建活动。
