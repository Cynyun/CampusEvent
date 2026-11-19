# 时序图：直接报名（UC-04）

**阶段**：初始版本设计，对应 `baseline-v1`  
**图形版本**：[直接报名时序图](diagrams/sequence-direct.drawio.svg)（draw.io 源文件见 `diagrams/CampusEvent-design.drawio`）

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

    S->>RS: register(studentId, activityId)
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
            RS-->>S: 拒绝：请勿重复报名
        else 无有效报名
            RS->>RR: countApproved(activityId)
            RR-->>RS: approvedCount
            RS->>A: hasCapacity(approvedCount)
            alt 名额已满
                A-->>RS: false
                RS-->>S: 拒绝：名额已满
            else 有名额
                A-->>RS: true
                RS->>P: decide(activity, approvedCount)
                P-->>RS: APPROVED
                RS->>RR: save(registration: APPROVED)
                RR-->>RS: saved
                RS->>EP: publish(RegistrationApproved)
                EP->>O: onEvent(event)
                O->>O: write Message
                Note over O: 通知失败只记录日志，不回滚报名
                RS-->>S: 报名成功
            end
        end
    end
```

## 说明

- 容量检查、重复检查、活动开放检查是公共服务规则；`RegistrationPolicy` 只负责返回 `APPROVED`。
- 先保存并提交报名，再发布事件；通知属于提交后的副作用。
- 并发情况下事务开始对 Activity 行执行 SELECT ... FOR UPDATE，再重新计数，失败方按“名额已满”处理。
