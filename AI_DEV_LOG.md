# AI 开发日志（AI_DEV_LOG）

**项目**：CampusEvent 校园活动报名与管理系统  
**实验**：《软件工程》实验二  
**记录范围**：从设计评审、初始版编码、双数据库改造、云部署，到 baseline-v1 封存与 change-v2（CR-01/CR-02）实施  
**记录原则**：只记录对结果有影响的关键指令、AI 产出、人工判断和问题修复；不粘贴整段对话，不把“AI 说完成了”当作验收证据。  
**执行主体**：DSH（本对话中的 AI 编码代理）、用户（需求、评审、部署与最终验收）、TRea（实验一样式调整使用，实验二未参与业务代码）。

## 1. 项目与基线

- 初始需求：P2-F01—P2-F08；验收 C1—C10。
- 设计任务：类图、策略模式（RegistrationPolicy 两种实现）、观察者/事件解耦通知、设计评审签字后才能编码。
- 技术栈：Spring Boot 3.3.5、Java 21、MyBatis、SQLite（默认）/ MySQL 8、Vue 3 + Vite + Element Plus。
- 代码基线：baseline-v1（46b5c3f），保留 15 项测试与初始设计；change-v2（2026-11-26）在基线之上实现 CR-01/CR-02，测试增至 24 项。
- 公网地址：https://cynyun-campus.thevibehosting.com/ （2026-11-21 起可访问；演示账号 student/123456、admin/123456）。
- 变更状态：教师 CR-01（年级限定直接报名）与 CR-02（模拟邮件通知）已发布、实施并部署在 change-v2 分支（`fdd2364`）；baseline-v1 的 main 与标签未动。

## 2. 关键指令与 AI 执行台账

| 日期/阶段 | 用户指令（原话或摘要） | AI 执行 | 人工判断/验收 | 产出 |
|---|---|---|---|---|
| 2026-11-19 设计 | 按实验二要求完成用例、类图、时序图与设计评审；先设计后编码 | 生成 01 用例说明、02 类图、03—05 三张时序图、00 设计评审表；用 draw.io 出 5 页图与 SVG | 用户与评审人确认 U1 软删除、U3 事务行锁；设计者/评审人签字后才进入编码 | docs/design/01—05、CampusEvent-design.drawio、5 个 SVG |
| 2026-11-19 设计决策 | 对 U1—U5 给出可选方案，必须说明取舍 | 给出 U1（物理删除/软删除）、U3（乐观锁/行锁）等选项、代价与推荐；U2/U4/U5 标为非阻塞 | 用户选择 U1=B 软删除、U3=A 行锁；U2/U4/U5 接受为已知边界 | 00-design-review.md、02-class-diagram.md |
| 2026-11-19 编码 | 在 D:/SE/CampusEvent 生成后端与前端，范围只做 P2-F01—P2-F08 | 生成 52 个生产 Java 文件：controller/service/mapper/policy/event/config；Vue 页面、路由、API 封装 | 用户检查页面与接口；人工确认演示账号、状态机、名额口径 | campusevent-server、campusevent-web |
| 2026-11-21 双数据库 | 默认 SQLite，可选 MySQL；前端 dist 放进 Spring Boot；首次启动写入演示数据 | 增加 application-sqlite.yml / application-mysql.yml、schema-sqlite.sql / schema-mysql.sql；MyBatisConfig 按方言切 databaseId；DemoDataSeeder 首次写 8 活动/36 学生/116 报名/116 通知；SpaController 做前端路由 fallback | 用户要求“不覆盖已有数据”；人工核对首次 8/37/116/116、重启不重复、MySQL profile 不重复初始化 | 提交 cfd96c1（44 files） |
| 2026-11-21 部署 | 部署到 ops vibe hosting，手机 15770660335 | 调用 Vibe Hosting API；手机验证被拒后改邮箱+$1 验证；创建 Ubuntu 24.04 服务器；安装 JDK 21、上传 jar、写 run.sh、supervisor 拉起 | 用户完成 $1 支付与邮箱验证；人工确认公网 HTTP 200、student01 登录、8 个活动 | srv_46480a4647、cynyun-campus.thevibehosting.com |
| 2026-11-21 扩容 | 服务器内存太小，能否扩到 2GB/4GB | 查账户与服务器端点；调用 refresh-ram；返回 ram_ceiling_mb=4096、old_mb=256、restarted=true | 用户确认账单/额度；人工用 free -m 核对 total 4096 MB | 4GB 服务器 |
| 2026-11-21 故障 | 浏览器 admin 登录报 403 | 看 DevTools 请求；用 curl 对比无 Origin/带 Origin；定位 WebConfig CORS 只允许 localhost:5173 | 人工确认不是后台没启动；AI 修改 allowedOriginPatterns 并重新打包上传 | 提交 6cf54c2；POST login 200 role=ADMIN |
| 2026-11-21 文档 | 更新 git 和 README，说明 403 原因 | 在 README 增加 CORS 允许列表、换域名同步修改、curl 验证示例 | 用户要求保留可复查的排错过程 | 提交 cd3b28e |
| 2026-11-23 进度检查 | 阅读实验指导书，检查实验二完成进度 | 抽取指导书 202 段/27 表；对照 3.2—3.8 列出缺口：无 tag、测试不足、无 AI_DEV_LOG/决策报告/初始 SRS/回归表/总结 | 用户确认“老师的 CR 还未发布”，要求先补齐初始版封存包 | 缺口清单 |
| 2026-11-23 封存 | “老师的CR还未发布 按上面 1—7 补齐初始版封存包” | 新增 C1—C10 自动化验收与真实重启测试；写初始 SRS、设计模式决策报告、回归测试表、本日志；更新设计 README/评审状态/根 README；清理 IDEA 生成的重复类图；跑全量测试；打 baseline-v1 | 用户逐项确认 1—7；人工复核测试输出与文档口径 | baseline-v1 |
| 2026-11-26 CR 分析与设计 | 发布 CR-01/CR-02，要求先分析影响再改代码 | 写《变更影响分析》；在类图/三张时序图加 CR 标注与变更区；保持 V1 文档原文，新增 change-v2 章节 | 用户确认图示修改点；人工判断不改审核型年级判断、邮件失败不回滚 | docs/变更影响分析.md、design/00—05 |
| 2026-11-26 CR 编码 | 按确认后的设计实现 CR-01/CR-02 | 新增 RegistrationContext、GradeLimitedDirectRegistrationPolicy、SchemaMigration；扩展 Factory；新增 mail/ 包、EmailNotificationObserver、MockEmailSender、EmailLog Mapper；改 RegistrationService 注入 StudentMapper | 人工核对年级 2/3 允许、1/4/空 403、满员 409；邮件仅 SUBMITTED/APPROVED/REJECTED | 24 项测试通过 |
| 2026-11-26 CR 测试 | 跑全量回归并补 CR 验收 | 新增 CR1/CR2 验收、EmailFailureTest、6 项年级限定单测；更新前端 dist；mvn test 24 项 | 人工看断言与日志表字段；确认重复报名不重复写 email_log | docs/回归测试表.md |

## 3. AI 承担的任务与人工边界

### 3.1 AI 主要承担

- 把指导书条目转成用例、类图、时序图、字段与业务规则草案。
- 生成 controller/service/mapper/entity/policy/event/config 与 Vue 页面、路由、API 封装。
- 生成测试脚手架、C1—C10 验收测试、真实重启测试、演示数据初始化。
- 整理部署命令、排错信息、README 与过程文档初稿。
- 对重复性、结构性工作提速：模板、CRUD、状态分支、文档表格。
- CR 阶段：把变更点落到策略/上下文/观察者与迁移脚本，补年级、邮件成功/失败、幂等测试。

### 3.2 必须由人完成

- 范围控制：拒绝注册、多人协作、真实邮件短信、复杂日历等范围外功能；CR-02 只做模拟邮件与 email_log。
- 设计决策：U1 软删除、U3 行锁、U2/U4/U5 接受为边界；这些不是 AI 能替用户承担的签子。
- 验收口径：名额只算 APPROVED、PENDING 不占名额、批准必复查、通知失败不回滚；CR1 年级限定只作用于直接报名，CR2 邮件失败只记日志，逐条对照 CR 确认。
- 线上操作：Vibe Hosting 的邮箱/支付、服务器凭据、SSH 私钥、公网验证由用户掌握；AI 不代替用户做生产变更。
- 最终判断：AI 产出是否真正满足 C1—C10，不以“代码能跑”或“页面能开”代替。

## 4. 问题与修复台账

| # | 现象 | 原因 | 修复/处理 | 证据 |
|---|---|---|---|---|
| 1 | Vibe Hosting 手机验证返回 400：phone verification isn't available for this country yet | 短信服务不支持该地区 | 改邮箱验证 + 一次性 $1；账户转 verified | 账户 tier=verified |
| 2 | 服务器初始仅 256 MB，Java 启动吃紧 | 默认规格小 | POST refresh-ram 扩到 4096 MB；512 MB 堆、G1、Metaspace 192m、Tomcat max 50 | free -m total 4096 MB |
| 3 | 浏览器 admin 登录 403，curl 却 200 | WebConfig CORS 只允许 localhost:5173；同源 POST 带公网 Origin 被拒 | allowedOriginPatterns 加公网域名与 *.thevibehosting.com；重新打包上传 | 带 Origin POST login 200 role=ADMIN |
| 4 | git push 报 schannel SSL/TLS 握手失败 | 本机代理/证书链 | git -c http.sslBackend=openssl -c http.version=HTTP/1.1 push | origin/main 同步 |
| 5 | 验收测试首轮 401/404，session 为 null | app.seed-demo-data=false 只建 student/admin，不建 student02—04 | 测试 @BeforeAll 用 JdbcTemplate 插入额外学生，密码哈希与演示账号一致 | 15 tests 全通过 |
| 6 | 重启测试仍占用 8080，未随机端口 | .properties 优先级低于 application.yml | 改用 SpringApplicationBuilder.run("--server.port=0", ...) | RestartTest 通过 |
| 7 | 设计 README 仍写“尚未开始 Java 实现” | 文档停留在编码前基线 | 更新为 V1 已实现、baseline-v1 已封存；补代码路径/测试入口；保留“不含 CR 变更” | design/README.md |
| 8 | 设计评审表状态写“签名待填写”，但签名已填 | 状态行未随签字更新 | 修正为“已签字”；追加实现后回填说明，原检查表保留 | 00-design-review.md |
| 9 | 验收测试注释说真正重启由 PowerShell 脚本完成 | 后来新增了 CampusEventRestartTest，注释未同步 | 文档指向 CampusEventRestartTest；注释在封存前修正 | 测试源码 |
| 10 | 通知没有幂等键/重试，崩溃窗口可能丢通知 | 采用 afterCommit + 尽力而为，未上 outbox | 明确写为 U2 已知边界；不用“不会重复”掩盖“可能丢失” | 设计模式决策报告 3.4 |
| 11 | 老 SQLite 库没有 student.grade，CR1 读取为空 | baseline-v1 建表脚本没有该列 | SchemaMigration 启动时检查列并 ALTER TABLE；DemoDataSeeder 补演示数据 grade | 24 项测试从空库与老库均可启动 |
| 12 | RegistrationPolicy 从 decide(approvedCount, capacity) 改为 decide(RegistrationContext) 后编译连锁报错 | 接口签名变化影响两个旧策略、Factory、Service 与单测 | 一次性改完调用点；旧策略从 context 取 approvedCount/capacity；单测统一构造 context | mvn test 编译通过 |
| 13 | 重复报名可能重复写邮件日志 | 邮件观察者若只按事件类型订阅，409 前已有事件或重复提交会重放 | 报名服务仍以有效报名唯一性拦截；观察者只处理提交后事件；CR2 断言重复报名 409 后 email_log 仍 1 条 | cr2EmailLogsAreWrittenAndDuplicateActionsDoNotRepeatThem |
| 14 | 前端 dist 未更新会与后端 CR 字段不一致 | 只改后端、忘记重新构建静态资源 | 重新 npm run build，清空并复制 dist 到 src/main/resources/static | 静态资源包含最新构建 |

## 5. 验证记录

- 命令：E:/apache-maven-3.9.12/bin/mvn.cmd -f D:/SE/CampusEvent/campusevent-server/pom.xml test，JAVA_HOME=E:/java/jdk-21。
- 结果：CampusEventAcceptanceTest 12/12、CampusEventEmailFailureTest 1/1、CampusEventRestartTest 1/1、RegistrationPolicyTest 9/9、PasswordUtilTest 1/1，合计 24 tests，0 failures，0 errors，BUILD SUCCESS。
- 真实 HTTP 冒烟（本地 jar + 临时 SQLite，18080）：创建活动 200；直接报名 APPROVED；重复报名 409；满员 409；审核报名 PENDING；批准 200；超员批准 409；取消待审核/已通过 200；统计同步；重启后数据仍在。
- CR1 冒烟：GRADE_LIMITED_DIRECT 活动 grade=1/4 报名 403、grade=2/3 报名 200 APPROVED。
- CR2 冒烟：直接报名 email_log SENT；重复报名 409 不新增日志；审核提交 PENDING 不发；批准/拒绝各 1 条；mock 失败写 FAILED 且报名不回滚。
- 公网：根路径 HTTP 200；未登录 API 401；student01/123456 可登录并看到既有活动；admin 登录在 CORS 修复后可进入管理端。change-v2 部署后于 2026-11-26 复测：CR1 四账号 403/200/200/403，CR2 直接报名与批准写 SENT、PENDING 不写，原 DIRECT 报名仍 200；详见 docs/验收记录.md。
- 证据文件：docs/回归测试表.md、docs/验收记录.md、docs/测试输出（如已保存）、docs/screenshots/。

## 6. AI 使用反思

1. **AI 最快的环节**是结构化产出：把需求拆成表、把状态机写成测试、把重复页面和 CRUD 写全。这些工作人工做也会，但慢且容易漏。
2. **AI 最容易失真的环节**是把“代码能跑”说成“需求已验收”。本项目里，C1—C10 的每条断言、名额口径、通知失败语义都必须人工对照指导书确认。
3. **设计模式不能被 AI 用来“显得专业”**。策略和观察者是因为存在两条真实变化轴才引入；如果只写“用了模式”而不写代价、备选方案和不适用场景，报告就没有工程价值。
4. **部署类工作必须区分“AI 建议”与“用户执行”**。账号、支付、SSH 私钥、服务器变更由用户掌握；AI 可以提供命令和排错路径，但不能替用户宣称生产环境已验收。
5. **文档要允许写“不知道/没验证”**。CR2 已用 `app.mock-email.fail=true` 做了邮件失败故障注入；真实 SMTP、MySQL 全量回归、前端自动化仍未验证，这类边界写出来比伪装成“全部验证”更可靠。
6. **CR 阶段最容易漏的是“旧接口的隐性调用点”**。把 `decide(int,int)` 改成 `decide(context)` 时，编译器能抓到的调用点不是全部风险；旧库列缺失、Factory 分支、测试构造器都要一起改，24 项测试比单看编译通过更可靠。

## 7. change-v2 收尾与后续

1. 已完成：从 baseline-v1 切出 change-v2；未在 main 直接改；CR 代码、测试、设计标注、文档改动均保留在分支。
2. 已完成：跑 24 项测试（C1—C10 + CR1/CR2 + 邮件失败 + 重启 + 策略/密码）；更新回归测试表、验收记录、README、设计报告与变更影响分析。
3. 已完成：commit/push change-v2（`fdd2364`）；构建 jar 并部署 Vibe；公网验证 CR1 403/200、CR2 email_log、PENDING 不发信与原 DIRECT 回归；部署记录已补入验收记录，提交后打 `change-v2` 标签。
4. 后续可做：真实 SMTP、MySQL 全量回归、前端 Playwright/Cypress、通知重试/幂等键。
5. baseline-v1 的 main 与标签保持不动；change-v2 的结论以本分支文档和部署复测为准。
