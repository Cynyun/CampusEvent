# 时序图：审批报名（UC-07）

**阶段**：设计 V1 基线 + change-v2 CR 标注（绿色=新增、黄色=修改；V1 快照见 `baseline-v1`）  
**图形版本**：[审批报名时序图](diagrams/sequence-approval.drawio.svg)（draw.io 源文件见 `diagrams/CampusEvent-design.drawio`）

```mermaid
sequenceDiagram
    autonumber
    actor A as Admin
    participant RS as RegistrationService
    participant RR as RegistrationRepository
    participant AC as Activity
    participant EP as RegistrationEventPublisher
    participant O as InAppNotificationObserver

    A->>RS: approveOrReject(adminId, registrationId, action)
    RS->>RR: findById(registrationId)
    alt 报名不存在
        RR-->>RS: null
        RS-->>A: 404 / 报名不存在
    else 状态不是待审核
        RR-->>RS: registration
        RS-->>A: 拒绝：状态已变化
    else 待审核
        RR-->>RS: registration
        alt 管理员选择拒绝
            RS->>RR: updateStatus(REJECTED)
            RS->>EP: publish(RegistrationRejected)
            EP->>O: onEvent(event)
            O->>O: write Message
            RS-->>A: 已拒绝
        else 管理员选择批准
            RS->>RR: countApproved(activityId)
            RR-->>RS: approvedCount
            RS->>AC: hasCapacity(approvedCount)
            alt 名额已满
                AC-->>RS: false
                RS-->>A: 批准失败：名额已满
            else 有名额
                AC-->>RS: true
                RS->>RR: updateStatus(APPROVED)
                RS->>EP: publish(RegistrationApproved)
                EP->>O: onEvent(event)
                O->>O: write Message
                RS-->>A: 批准成功
            end
        end
    end
```

## 说明

- 审批前必须重新检查“待审核”状态，避免重复审批。
- 批准时再次检查容量；名额已满时状态保持“待审核”，不超上限。
- 并发批准最后一个名额时，事务开始对 Activity 行执行 SELECT ... FOR UPDATE，再重新计数；只有一个成功。

## change-v2 图示变更点

- **CR-02**：批准/拒绝都会产生事件并发送邮件；邮件失败只记 `EmailLog`，不回滚审批。
- **CR-01**：审批主流程不增加年级判断；年级限制仅在直接报名策略。
- 重复审批由 `PENDING` 状态约束；图示不新增幂等键。
- 本文件 Mermaid 仍为 V1 主流程；change-v2 以 SVG 底部“CR-01/CR-02 变更”说明为准。
