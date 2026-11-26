# 时序图：审核型报名（UC-05）

**阶段**：设计 V1 基线 + change-v2 CR 标注（绿色=新增、黄色=修改；V1 快照见 `baseline-v1`）  
**图形版本**：[审核型报名时序图](diagrams/sequence-review.drawio.svg)（draw.io 源文件见 `diagrams/CampusEvent-design.drawio`）

```mermaid
sequenceDiagram
    autonumber
    actor S as Student
    participant RS as RegistrationService
    participant AR as ActivityRepository
    participant RR as RegistrationRepository
    participant A as Activity
    participant P as RegistrationPolicy
    participant EP as RegistrationEventPublisher
    participant O as InAppNotificationObserver

    S->>RS: apply(studentId, activityId)
    RS->>AR: findById(activityId)
    AR-->>RS: activity
    RS->>A: isOpen(now)
    alt 活动已开始
        A-->>RS: false
        RS-->>S: 拒绝：报名已截止
    else 活动开放
        A-->>RS: true
        RS->>RR: findActive(studentId, activityId)
        alt 存在有效报名
            RR-->>RS: registration
            RS-->>S: 拒绝：请勿重复申请
        else 无有效报名
            RS->>P: decide(activity, approvedCount)
            P-->>RS: PENDING
            RS->>RR: save(registration: PENDING)
            RR-->>RS: saved
            RS->>EP: publish(RegistrationSubmitted)
            EP->>O: onEvent(event)
            O->>O: write Message
            Note over O: 通知失败只记录日志，不回滚报名
            RS-->>S: 申请成功，等待审核
        end
    end
```

## 说明

- 审核型报名提交时**不扣减名额**，待审核不占容量。
- 活动已满员时仍允许提交待审核申请；审批时再检查容量（UC-07）。
- 有效报名指状态为“待审核”或“已通过”；已取消/已拒绝允许再次申请。

## change-v2 图示变更点

- **CR-01**：审核型报名提交时同样构造 `RegistrationContext`；`ApprovalRegistrationPolicy` 返回 `PENDING`；年级限制只作用于直接报名。
- **CR-02**：待审核阶段不发邮件；批准/拒绝后由 `EmailNotificationObserver` 发邮件。
- 图示保留 V1 主流程，底部加变更说明，不重画整图。
- 本文件 Mermaid 仍为 V1 主流程；change-v2 以 SVG 底部“CR-01/CR-02 变更”说明为准。
