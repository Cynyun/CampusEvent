# CampusEvent 设计文档索引（V1）

**项目**：校园活动报名与管理系统（CampusEvent）  
**阶段**：编码前设计基线；尚未开始 Java 实现  
**日期**：2026-11-19  
**对应实验**：《软件工程》实验二 3.1—3.8 与附录 B

本目录只放“设计 V1”的用例、类图、时序图、评审表和图形源文件。教师统一发布 CR 之前，不实现变更需求；本目录也不包含业务代码。

## 1. 文件索引

| 文件 | 内容 | 用途 |
|---|---|---|
| 01-use-case-description.md | 9 个用例、参与者、前置/基本流/扩展流/后置、业务规则 | 需求到设计的入口；评审时逐条对照 P2-F01—F08 |
| 02-class-diagram.md | 语言中立 UML 类图（Mermaid）、17 个类型、职责、策略/观察者/并发决策、用例追踪 | 编码前的静态结构基线 |
| 03-sequence-direct-registration.md | 直接报名成功主流程与失败说明 | 验证直接报名、容量、通知的协作 |
| 04-sequence-review-registration.md | 审核型报名成功主流程与失败说明 | 验证待审核不占名额、满员仍可申请 |
| 05-sequence-approval.md | 批准成功主流程；拒绝分支、满员失败 | 验证批准复查容量、状态防重、通知 |
| 00-design-review.md | 22 项检查表、决策记录、评审意见与签字栏 | 编码前的评审门禁 |
| diagrams/CampusEvent-design.drawio | 5 页 draw.io 源文件 | 需要改图时编辑此文件 |
| diagrams/use-case.drawio.svg | 用例图 SVG | 文档展示/打印 |
| diagrams/class-diagram.drawio.svg | 类图 SVG | 文档展示/打印 |
| diagrams/sequence-direct.drawio.svg | 直接报名时序 SVG | 文档展示/打印 |
| diagrams/sequence-review.drawio.svg | 审核报名时序 SVG | 文档展示/打印 |
| diagrams/sequence-approval.drawio.svg | 审批时序 SVG | 文档展示/打印 |

## 2. 建议阅读顺序

1. **01-use-case-description.md**：先确认范围、参与者和 9 个用例没有越界。
2. **02-class-diagram.md**：看实体、服务、策略、通知四层职责是否分开。
3. **03-sequence-direct-registration.md**：跟一遍直接报名的成功路径和失败分支。
4. **04-sequence-review-registration.md**：确认待审核不占名额。
5. **05-sequence-approval.md**：确认批准时复查容量、拒绝不检查容量。
6. **00-design-review.md**：拿检查表逐项打勾，核对决策记录并签名。
7. **diagrams/CampusEvent-design.drawio**：需要放大细节或改图时打开；SVG 只用于展示。

## 3. 需求到设计的追踪

### 3.1 功能需求

| 需求 | 用例 | 主要类/接口 | 时序图 |
|---|---|---|---|
| P2-F01 活动 CRUD | UC-01、UC-02 | Admin、Activity、ActivityRepository | 无（用例说明足够） |
| P2-F02 浏览与筛选 | UC-03 | Activity、ActivityRepository | 无 |
| P2-F03 报名/取消 | UC-04、UC-05、UC-06 | RegistrationService、Registration、RegistrationPolicy | 03、04 |
| P2-F04 直接/审核两种方式 | UC-04、UC-05 | PolicyType、DirectRegistrationPolicy、ApprovalRegistrationPolicy | 03、04 |
| P2-F05 审批 | UC-07 | RegistrationService、Registration、Activity | 05 |
| P2-F06 四状态 | UC-04—UC-07 | RegistrationStatus、Registration | 03、04、05 |
| P2-F07 站内通知 | UC-08 | RegistrationEventPublisher、NotificationObserver、Message | 03、04、05 |
| P2-F08 统计 | UC-09 | RegistrationService、Activity | 05（容量复查） |

### 3.2 验收用例 C1—C10

| 验收 | 场景 | 设计支撑 |
|---|---|---|
| C1 | 管理员创建容量 2 的直接报名活动 | UC-01；Activity.capacity/policyType；类图 Activity |
| C2 | 学生 A 直接报名成功并收到通知 | UC-04；03 时序；DirectRegistrationPolicy → APPROVED；发布事件 |
| C3 | A 重复报名被拒绝 | UC-04 E1；Service 查重；同活动仅一条有效报名 |
| C4 | B 占满名额，C 报名失败 | UC-04 E2/E5；countApproved + hasCapacity；并发说明 |
| C5 | 审核型活动 A 报名后为待审核 | UC-05；ApprovalRegistrationPolicy → PENDING；不占名额 |
| C6 | 管理员批准 A，A 通过并收到通知 | UC-07；05 时序批准分支 |
| C7 | 管理员拒绝另一人并通知 | UC-07 拒绝分支；updateStatus(REJECTED) → publish |
| C8 | 名额已满时批准失败 | UC-07 满员分支；状态保持 PENDING |
| C9 | 取消待审核/已通过，统计同步 | UC-06；取消后不计入 countApproved |
| C10 | 重启后数据一致 | 仓储持久化；Service 不依赖内存状态；实现阶段补集成测试 |

## 4. 关键设计决策（摘要）

1. **策略只决定报名结果**：RegistrationPolicy 只返回 APPROVED/PENDING；容量、开放时间、重复报名由 RegistrationService 统一校验。避免两个策略各写一套容量规则。
2. **名额只算已通过**：countApproved 只统计 APPROVED；PENDING 不占名额，所以审核型活动可以超额提交申请，批准时再复查容量。
3. **通知与事务解耦**：先提交报名/审批状态，再 publish；InAppNotificationObserver 写 Message；通知失败只记日志，不回滚业务。
4. **防重复**：状态转换只能发生一次；审批前先检查 PENDING；同一状态变化不重复发布事件。
5. **不引入 MQ/分布式事务**：课程范围用同步观察者；报告里说明“提交后发布前崩溃可能丢通知”的边界。
6. **语言中立类图**：先按 UML 画职责与关系，Java 实现时再映射到具体类型；后续可用 IDEA 从代码生成类图做对比。

## 5. 图形维护约定

- **文字版（Mermaid）是评审与 diff 的主版本**；draw.io 是展示版，两者必须同步。
- 改图时编辑 diagrams/CampusEvent-design.drawio，不要手工改 .drawio.svg。
- draw.io 文件共 5 页：01 用例图、02 类图、03 直接报名、04 审核报名、05 审批；改完某页后重新导出对应 SVG。
- 类图保持语言中立：不写 Java 注解、包名、框架类型；这些留到实现阶段。
- 时序图只画协作与消息顺序，不画具体 SQL；持久化细节放在实现和测试里。

## 6. 已决策与剩余问题

- **U1 删除有报名的活动**：已关闭，选 B 软删除。Activity 增加 deletedAt；只置删除时间，不物理删除；学生列表与报名入口过滤已删除活动，历史报名与通知保留。落地约束见 00-design-review.md 第 3.1 节。
- **U3 名额并发控制**：已关闭，选 A 事务内行锁。报名与审批在事务开始时对 Activity 行执行 SELECT ... FOR UPDATE，再统计已通过人数；不做乐观锁重试。落地约束见 00-design-review.md 第 3.2 节。
- **U2（通知极端丢事件）、U4（活动开始后能否取消）、U5（管理员能否审批自己创建的活动）**：已在评审表记录，不阻塞编码，按 00-design-review.md 第 3 节默认方案执行。

## 7. 范围边界

- 本设计覆盖 P2-F01—P2-F08；不实现注册、复杂权限、邮件/短信、复杂日历、移动端 App。
- 不实现教师后续 CR；变更版类图、影响分析和 change-v2 标签在收到 CR 后单独做。
- 不包含 UI 视觉稿；页面结构在实现阶段按用例基本流落地。
- 本目录不含 Java 代码；代码进入 src/ 后再补实现映射与测试。
