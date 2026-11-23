# CampusEvent 初始 SRS 分析（V1）

**项目**：校园活动报名与管理系统（CampusEvent）  
**实验**：《软件工程》实验二 3.2 初始业务需求与附录 B  
**文档版本**：SRS-V1，对应代码基线 **baseline-v1**  
**编写日期**：2026-11-23  
**需求来源**：教师实验指导书 3.2、3.3、3.5、附录 B.1/B.2；项目设计文档 01—05；已实现代码与 C1—C10 验收记录  
**变更状态**：教师 CR 尚未发布；本文件只描述初始版，不包含、也不提前实现 change-v2 的任何需求

> 说明：本文件是“初始 SRS 分析”，不是对教师指导书的逐字复述。它把指导书中的功能条目、领域字段、业务规则和验收用例整理成可追踪、可测试的需求基线，并明确哪些内容属于实现补充、哪些属于已知限制。

## 1. 项目目标与范围

### 1.1 目标

为校园活动提供一个单机可运行、可公网访问的报名与管理系统。管理员发布活动并维护报名方式；学生浏览活动、提交报名或取消报名；系统按“直接报名”或“管理员审核”两类规则处理报名，写入站内通知，并提供已通过人数、待审核人数、剩余名额统计。

### 1.2 角色

| 角色 | 说明 | 使用方式 |
|---|---|---|
| Student 学生 | 浏览活动、报名、取消、查看自己的报名与站内通知 | 固定演示账号 student / 123456；批量账号 student01—student36 |
| Admin 管理员 | 创建、编辑、软删除活动；审批待审核报名；查看统计 | 固定演示账号 admin / 123456 |

不实现用户注册、找回密码、复杂权限、多管理员审批链。角色通过 HttpSession 和拦截器区分，管理员接口与学接口分别校验角色。

### 1.3 功能范围

| 编号 | 功能 | 初始版要求 | 本版实现要点 |
|---|---|---|---|
| P2-F01 | 活动管理 | 管理员创建、编辑、删除活动，设置名称、类型、时间、地点、人数上限、报名方式 | 创建/编辑校验开始时间与容量；删除采用软删除，保留历史报名与通知 |
| P2-F02 | 活动查看 | 学生浏览活动列表和详情，可按名称筛选 | 支持名称关键词、报名方式、分类筛选；详情显示名额与自己的报名状态 |
| P2-F03 | 报名/取消 | 学生报名、取消；禁止同一活动出现重复有效报名 | 同一学生同一活动只允许一条 PENDING/APPROVED；取消后保留历史 |
| P2-F04 | 报名策略 | 直接报名自动通过；审核型报名进入待审核 | RegistrationPolicy 两种实现；策略只决定新提交的准入结果 |
| P2-F05 | 审批操作 | 管理员批准或拒绝待审核申请，批准时再次检查名额 | 批准前锁活动行并重新统计 APPROVED；满员返回 409，状态保持 PENDING |
| P2-F06 | 状态管理 | 待审核、已通过、已拒绝、已取消；状态转换符合规则 | 只允许合法转换；重复审批/重复取消被拒绝 |
| P2-F07 | 站内通知 | 报名结果及审核结果写入站内消息列表 | 报名提交、通过、拒绝、取消各写一条；通知在事务提交后发布 |
| P2-F08 | 统计 | 按活动查看已通过数、待审核数和剩余名额 | 只统计 APPROVED 占用名额；PENDING 单列显示 |

### 1.4 明确不做

用户注册与找回密码、复杂 RBAC、多人协作、邮件/短信通知、复杂日历、移动端 App、消息队列、分布式事务、生产级备份/监控/限流。活动列表暂不分页。

## 2. 领域对象与数据约束

### 2.1 对象与字段

| 对象 | 主要字段 | 约束与说明 |
|---|---|---|
| Student | student_id、name、password_hash | student_id 唯一；password_hash 为演示登录补充字段，不属于指导教师建议字段 |
| Admin | admin_id、name、password_hash | 固定演示管理员；password_hash 为实现补充 |
| Activity | activity_id、title、category、start_at、location、capacity、policy_type、deleted_at、created_at、updated_at | capacity 为正整数；创建时 start_at 必须晚于当前时间；capacity 不得小于已通过人数；deleted_at 非空表示软删除 |
| Registration | registration_id、activity_id、student_id、status、created_at、updated_at | status 只能是 PENDING / APPROVED / REJECTED / CANCELLED；同一学生同一活动最多一条 PENDING/APPROVED |
| Message | message_id、student_id、activity_id、content、created_at、is_read | 报名或审批动作可产生；is_read 默认为 false |

技术表 app_meta 只保存初始化标记（demo_seeded），不是业务领域对象。

### 2.2 关系

- 一个 Activity 有多条 Registration；一条 Registration 只属于一个 Activity 和一个 Student。
- 一个 Student 有多条 Registration、多条 Message。
- Message 通过 student_id 归属学生，通过 activity_id 关联活动；活动被软删除后消息仍保留。
- Registration 的历史状态（REJECTED/CANCELLED）保留，用于再次申请前的判断和统计追溯。

## 3. 业务规则

| 编号 | 规则 | 违反时的处理 |
|---|---|---|
| BR-01 | 活动容量必须为正整数；创建时开始时间必须晚于当前时间 | 返回 400，提示具体字段 |
| BR-02 | 编辑活动时容量不得小于当前已通过人数；修改开始时间时新时间必须晚于当前时间 | 返回 400 |
| BR-03 | 活动开始后不允许新增报名 | 返回 400“活动已开始，无法报名” |
| BR-04 | 同一学生同一活动不得存在两条 PENDING/APPROVED | 返回 409“你已报名该活动，请勿重复提交” |
| BR-05 | 直接报名：名额充足时创建 APPROVED；名额已满时拒绝 | 返回 409“活动名额已满” |
| BR-06 | 审核型报名：提交后为 PENDING，不占用名额 | 返回 200，status=PENDING |
| BR-07 | 批准前再次检查名额；名额已满时不得批准 | 返回 409，报名保持 PENDING，不超上限 |
| BR-08 | 状态只允许 PENDING→APPROVED/REJECTED/CANCELLED，APPROVED→CANCELLED；REJECTED/CANCELLED 为终态，可重新申请 | 非法转换返回 409 |
| BR-09 | 学生只能取消自己的 PENDING/APPROVED；活动开始后仍允许取消（U4） | 非本人或状态不符返回 404/409 |
| BR-10 | 删除活动采用软删除，不级联删除报名与通知；学生端过滤已删除活动 | 管理员可查看“显示已删除”；历史数据保留 |
| BR-11 | 通知在业务事务提交后发布；通知失败只记录日志，不回滚已合法提交的报名 | 业务成功但消息可能缺失；不重试 |
| BR-12 | 名额统计只计算 APPROVED；PENDING 不占名额；剩余名额 = capacity - approvedCount | 统计接口与页面同口径 |
| BR-13 | MySQL 下报名/审批在事务内对 Activity 行执行 SELECT ... FOR UPDATE；SQLite 使用单连接池串行化写入 | 并发下不超卖；不做乐观锁重试 |
| BR-14 | 一次合法状态变更只发布一次事件，不设计重试；因此当前版本不会因重试产生重复通知，但“提交后、发布前”崩溃会丢通知（U2） | 记录为已知边界，不引入 outbox/MQ |

### 3.1 名额检查的责任划分

名额检查分两层：

1. **统计与并发控制属于公共服务职责**：RegistrationService 在事务内锁定 Activity 行，调用 ActivityMapper.countApproved，保证同一活动的报名/审批串行判断。
2. **“新提交应当得到什么状态”属于策略职责**：RegistrationPolicy 接收 approvedCount 与 capacity，DirectRegistrationPolicy 在提交时判断容量并返回 APPROVED，ApprovalRegistrationPolicy 始终返回 PENDING。
3. **“批准不能超过容量”属于服务层公共不变量**：approve 在更新状态前重新统计并比较，因为批准动作与直接报名共享同一条容量上限，不能由某个策略单独决定。

这样划分的原因：如果把容量判断全部塞进策略，审批型策略也必须重复实现“批准时复查”，且“待审核不占名额”的规则会被打散；如果把策略完全退化为常量，直接报名与审核报名的差异又会重新变成服务里的 if/else。当前划分让策略只回答“新提交的准入结果”，把跨用例的容量不变量留在服务层。

## 4. 用例

详细基本流、扩展流和前置/后置条件见 docs/design/01-use-case-description.md。本节给出 SRS 级摘要。

| 编号 | 用例 | 参与者 | 主成功场景 | 主要异常/后置 |
|---|---|---|---|---|
| UC-01 | 创建活动 | Admin | 填写名称、类型、时间、地点、容量、报名方式；校验通过后保存 | 时间不未来、容量非正数 → 400；成功后活动可被学生浏览 |
| UC-02 | 编辑/删除活动 | Admin | 修改活动字段；删除时写 deleted_at | 容量小于已通过人数 → 400；删除后学生端不可见，历史保留 |
| UC-03 | 浏览/筛选/详情 | Student | 按名称、报名方式、分类筛选；打开详情看名额与自己的状态 | 已删除/不存在 → 404 |
| UC-04 | 直接报名 | Student | 校验活动开放、未重复；容量足够则创建 APPROVED；发布通知 | 已开始/重复/满员 → 400 或 409；不产生报名 |
| UC-05 | 审核型报名 | Student | 校验活动开放、未重复；创建 PENDING；发布“待审核”通知 | 已开始/重复 → 400/409；PENDING 不占名额 |
| UC-06 | 取消报名 | Student | 校验本人且状态为 PENDING/APPROVED；改为 CANCELLED；发布通知 | 非本人/状态不符 → 404/409；统计同步减少 |
| UC-07 | 审批报名 | Admin | 锁定活动；确认报名仍为 PENDING；复查容量；改为 APPROVED 或 REJECTED；发布通知 | 满员 → 409 且保持 PENDING；非 PENDING → 409 |
| UC-08 | 查看站内通知 | Student | 查看消息列表、未读数；单条/全部标记已读 | 只能操作自己的消息 |
| UC-09 | 查看统计 | Admin | 按活动查看 approvedCount、pendingCount、remaining、使用率 | 已删除活动不计入统计 |

## 5. 非功能需求

| 编号 | 类别 | 要求 | 当前证据/限制 |
|---|---|---|---|
| NFR-01 | 可用性 | 学生端与管理员端页面为中文、响应式布局；关键操作有成功/失败提示 | Vue 3 + Element Plus；9 个页面；截图见 docs/screenshots |
| NFR-02 | 性能 | 课程演示规模下页面与接口可交互；列表与统计不出现明显卡顿 | 默认数据 8 个活动、36 个学生、116 条报名；未做压力测试 |
| NFR-03 | 安全 | 未登录访问业务接口返回 401；学生不能调用管理员接口；密码不以明文入库 | HttpSession + 拦截器；SHA-256；固定演示密码仅用于课程，不适合生产 |
| NFR-04 | 可靠性 | 重启后活动、报名、通知保持一致；初始化不重复插入 | SQLite 单文件；app_meta.demo_seeded；C10 自动化重启测试 |
| NFR-05 | 可维护性 | 控制层、服务层、策略、事件、持久化分层；业务规则有自动化测试 | 15 项自动化测试；策略/观察者结构见设计模式决策报告 |
| NFR-06 | 可部署性 | 一个 Java 进程同时提供页面与 API；可部署到云服务器 | jar + static；Vibe Hosting 公网地址已运行 |
| NFR-07 | 兼容性 | JDK 21；默认 SQLite，可切 MySQL 8 | application-sqlite.yml / application-mysql.yml；SQLite 不支持 FOR UPDATE，改用单连接串行化 |

## 6. 验收标准

| 编号 | 验收场景 | 预期结果 | 自动化覆盖 |
|---|---|---|---|
| C1 | 管理员创建直接报名活动，容量 2 | 创建成功，可查询详情 | CampusEventAcceptanceTest.c1CreateActivitiesAndReadThemBack |
| C2 | 学生 A 报名直接报名活动 | APPROVED，并生成“报名成功”通知 | c2DirectRegistrationIsApprovedAndCreatesMessage |
| C3 | 学生 A 再次提交相同活动 | 409，拒绝重复有效报名 | c3DuplicateRegistrationIsRejected |
| C4 | B 占满后 C 再报名 | B 通过，C 因名额不足 409 | c4CapacityLimitBlocksExtraRegistration |
| C5 | 审核型活动，A 申请 | PENDING，approvedCount 仍为 0 | c5ApprovalRegistrationStartsPendingAndDoesNotOccupySeat |
| C6 | 管理员批准 A | APPROVED，并生成“报名已通过”通知 | c6AdminApprovalChangesStatusAndNotifiesStudent |
| C7 | 管理员拒绝另一人 | REJECTED，并生成“报名未通过”通知 | c7AdminRejectionChangesStatusAndNotifiesStudent |
| C8 | 名额已满时批准 | 409，不超上限，状态保持 PENDING | c8ApprovalRechecksCapacityAndKeepsOverflowPending |
| C9 | 取消待审核/已通过 | CANCELLED，统计同步 | c9CancelApprovedAndPendingRegistrationsFreesSeats |
| C10 | 重启后查询活动、报名、通知 | 数据一致 | c10DataIsPersistedInTheSqliteFile；CampusEventRestartTest.restartKeepsActivitiesRegistrationsAndMessages |

验收执行结果、命令和证据见 docs/回归测试表.md 与 docs/验收记录.md。

## 7. 约束、假设与开放问题

### 7.1 约束

- 教师 CR 发布前不得获取或提前实现变更；本基线只覆盖初始版。
- 只使用固定演示账号，不实现注册与细粒度权限。
- 默认 SQLite 单连接，适合课程演示；高并发写入应切 MySQL。
- 公网部署为单机、按需休眠，未实现生产级备份、监控、限流与多实例容灾。

### 7.2 假设

- 活动开始时间使用服务器本地时间，学生与管理员在同一时区使用。
- 活动容量在报名期间可能被管理员调大或调小；调小时不得低于已通过人数。
- 通知是站内消息，不要求外部邮件/短信可达。
- 课程验收以接口行为、数据库状态和页面操作为证据，不要求压测数据。

### 7.3 开放问题

| 编号 | 问题 | 当前处理 | 影响 |
|---|---|---|---|
| U2 | 提交后、发布前进程崩溃会丢通知 | 接受课程范围内的极小概率；失败只记日志，不引入 outbox/MQ | 通知可能缺失，但报名不回滚 |
| U4 | 活动开始后能否取消 | 当前允许取消 PENDING/APPROVED；新增报名仍禁止 | 若教师要求截止后不可取消，需改 cancel 规则 |
| U5 | 管理员能否审批自己创建的活动 | 当前不限制；固定演示账号下无影响 | 多管理员场景需补权限规则 |
| Q1 | 活动列表是否需要分页 | 当前不分页 | 数据量大时需要补充分页与索引 |
| Q2 | 通知是否需要重试/去重表 | 当前不重试、不建 outbox | CR 若要求可靠通知，需要新增机制 |

## 8. 需求追踪

| 需求 | 用例 | 主要实现 | 验收 |
|---|---|---|---|
| P2-F01 | UC-01、UC-02 | ActivityService、AdminActivityController、ActivityMapper | C1、C8 前置活动创建 |
| P2-F02 | UC-03 | ActivityService.listActive、ActivityController | C1 详情查询 |
| P2-F03 | UC-04、UC-05、UC-06 | RegistrationService.register/cancel、RegistrationMapper | C2、C3、C9 |
| P2-F04 | UC-04、UC-05 | RegistrationPolicy、DirectRegistrationPolicy、ApprovalRegistrationPolicy、RegistrationPolicyFactory | C2、C5 |
| P2-F05 | UC-07 | RegistrationService.approve/reject、AdminRegistrationController | C6、C7、C8 |
| P2-F06 | UC-04—UC-07 | RegistrationStatus、状态前置检查 | C3、C6、C7、C8、C9 |
| P2-F07 | UC-08 | RegistrationEventPublisher、InAppNotificationObserver、MessageService | C2、C6、C7、C9、C10 |
| P2-F08 | UC-09 | ActivityService.statistics、ActivityStats | C5、C8、C9 |

## 9. 变更控制

- 本文档与代码基线 baseline-v1 对应；标签打在包含本文档、回归测试表、AI_DEV_LOG 和 15 项测试的提交上。
- 教师发布 CR 后，先提交变更影响分析（涉及类/接口、风险、预计新增测试、修改范围），再从 baseline-v1 建分支实现，保留 Git diff 与 AI 交互记录，跑全部 C1—C10 和新增用例，最后打 change-v2。
- 在 CR 发布前，不修改本文档的初始版需求，也不把“未来可能要做”的功能写成已实现。
