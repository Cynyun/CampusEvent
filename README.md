# CampusEvent 校园活动报名与管理系统

《软件工程》实验二项目。当前处于**设计 V1**阶段：已完成用例说明、UML 类图、报名/审批时序图、设计评审表与 draw.io 图，尚未开始 Java 业务代码。

## 当前状态

| 项 | 状态 |
|---|---|
| 需求范围 | P2-F01—P2-F08；Student/Admin 两种固定演示账号 |
| 设计文档 | 已完成，入口 docs/design/README.md |
| 设计评审 | 自检完成；U1/U3 已决策；学生互评签名待填写，见 docs/design/00-design-review.md |
| 代码 | 未开始；待学生互评签字后进入实现 |
| 教师 CR | 未发布，未提前实现 |
| 部署 | 未开始；实现完成后按实验要求部署云服务器 |

## 设计文档入口

- [设计文档索引](docs/design/README.md)：文件清单、阅读顺序、P2-F01—F08 与 C1—C10 追踪
- [用例说明](docs/design/01-use-case-description.md)：9 个用例与业务规则
- [UML 类图](docs/design/02-class-diagram.md)：语言中立类图、策略/观察者/并发决策
- [直接报名时序](docs/design/03-sequence-direct-registration.md)
- [审核型报名时序](docs/design/04-sequence-review-registration.md)
- [审批时序](docs/design/05-sequence-approval.md)
- [设计评审表](docs/design/00-design-review.md)：22 项检查、决策记录、签字栏
- [draw.io 源文件](docs/design/diagrams/CampusEvent-design.drawio)：5 页，可编辑

## 设计基线

- 用例：UC-01 创建活动、UC-02 编辑/删除、UC-03 浏览筛选、UC-04 直接报名、UC-05 审核报名、UC-06 取消、UC-07 审批、UC-08 通知、UC-09 统计。
- 类：Student、Admin、Activity、Registration、Message 五个实体；RegistrationService 应用服务；RegistrationPolicy 策略及两个实现；RegistrationEventPublisher/Observer 通知解耦。
- 关键规则：待审核不占名额；批准时复查容量；同一学生同一活动只能有一条有效报名；通知提交后发布，失败不回滚。
- 已决策：U1 删除有报名活动采用软删除（deletedAt）；U3 报名/审批采用事务内 SELECT ... FOR UPDATE 锁 Activity 行；落地约束见评审表 3.1/3.2。

## 范围外

注册登录、复杂权限、多人协作、邮件/短信、复杂日历、移动端 App、消息队列、分布式事务。

## 目录

    CampusEvent/
      README.md
      docs/design/
        00-design-review.md
        01-use-case-description.md
        02-class-diagram.md
        03-sequence-direct-registration.md
        04-sequence-review-registration.md
        05-sequence-approval.md
        README.md
        diagrams/
          CampusEvent-design.drawio
          use-case.drawio.svg
          class-diagram.drawio.svg
          sequence-direct.drawio.svg
          sequence-review.drawio.svg
          sequence-approval.drawio.svg

## 后续计划

1. 完成学生互评并签字；U1/U3 已决策，无需再关闭。
2. 按类图实现 Java 后端与页面，先跑 C1—C10。
3. 打 baseline-v1 标签。
4. 收到教师 CR 后做影响分析与变更版设计，打 change-v2。
5. 部署云服务器，补回归测试、AI_DEV_LOG 与项目总结。
