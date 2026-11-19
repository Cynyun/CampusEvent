# 时序图：审批报名（UC-07）

**阶段**：初始版本设计，对应 `baseline-v1`  
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
- 并发批准最后一个名额时，事务内重新计数/乐观锁；只有一个成功。
