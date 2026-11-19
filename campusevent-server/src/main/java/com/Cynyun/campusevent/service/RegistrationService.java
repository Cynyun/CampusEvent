package com.Cynyun.campusevent.service;

import com.Cynyun.campusevent.common.BusinessException;
import com.Cynyun.campusevent.common.TransactionHooks;
import com.Cynyun.campusevent.entity.Activity;
import com.Cynyun.campusevent.entity.Registration;
import com.Cynyun.campusevent.entity.RegistrationView;
import com.Cynyun.campusevent.enums.RegistrationEventType;
import com.Cynyun.campusevent.enums.RegistrationStatus;
import com.Cynyun.campusevent.event.RegistrationEvent;
import com.Cynyun.campusevent.event.RegistrationEventPublisher;
import com.Cynyun.campusevent.mapper.ActivityMapper;
import com.Cynyun.campusevent.mapper.RegistrationMapper;
import com.Cynyun.campusevent.policy.RegistrationPolicy;
import com.Cynyun.campusevent.policy.RegistrationPolicyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final ActivityMapper activityMapper;
    private final RegistrationMapper registrationMapper;
    private final RegistrationPolicyFactory policyFactory;
    private final RegistrationEventPublisher eventPublisher;

    @Transactional
    public Registration register(String studentId, Long activityId) {
        Activity activity = activityMapper.findByIdForUpdate(activityId);
        if (activity == null || activity.getDeletedAt() != null) {
            throw BusinessException.notFound("活动不存在或已删除");
        }
        if (!activity.getStartAt().isAfter(LocalDateTime.now())) {
            throw BusinessException.badRequest("活动已开始，无法报名");
        }
        Registration existing = registrationMapper.findActiveByStudentAndActivity(studentId, activityId);
        if (existing != null) {
            throw BusinessException.conflict("你已报名该活动，请勿重复提交");
        }

        int approvedCount = activityMapper.countApproved(activityId);
        RegistrationPolicy policy = policyFactory.create(activity.getPolicyType());
        RegistrationStatus status = policy.decide(approvedCount, activity.getCapacity());

        Registration registration = new Registration();
        registration.setStudentId(studentId);
        registration.setActivityId(activityId);
        registration.setStatus(status);
        registration.setCreatedAt(LocalDateTime.now());
        registration.setUpdatedAt(registration.getCreatedAt());
        registrationMapper.insert(registration);

        publishAfterCommit(registration, RegistrationEventType.SUBMITTED, status);
        return registration;
    }

    @Transactional
    public void cancel(String studentId, Long registrationId) {
        Registration snapshot = registrationMapper.findById(registrationId);
        if (snapshot == null || !snapshot.getStudentId().equals(studentId)) {
            throw BusinessException.notFound("报名记录不存在");
        }
        activityMapper.findByIdForUpdate(snapshot.getActivityId());

        Registration registration = registrationMapper.findByIdForUpdate(registrationId);
        if (registration == null || !registration.getStudentId().equals(studentId)) {
            throw BusinessException.notFound("报名记录不存在");
        }
        if (registration.getStatus() != RegistrationStatus.PENDING
                && registration.getStatus() != RegistrationStatus.APPROVED) {
            throw BusinessException.conflict("当前报名状态不可取消");
        }

        registrationMapper.updateStatus(registrationId, RegistrationStatus.CANCELLED, LocalDateTime.now());
        publishAfterCommit(registration, RegistrationEventType.CANCELLED, RegistrationStatus.CANCELLED);
    }

    @Transactional
    public void approve(Long registrationId) {
        Registration snapshot = registrationMapper.findById(registrationId);
        if (snapshot == null) {
            throw BusinessException.notFound("报名记录不存在");
        }
        Activity activity = activityMapper.findByIdForUpdate(snapshot.getActivityId());
        if (activity == null || activity.getDeletedAt() != null) {
            throw BusinessException.conflict("活动已删除，无法审批");
        }

        Registration registration = registrationMapper.findByIdForUpdate(registrationId);
        if (registration == null || registration.getStatus() != RegistrationStatus.PENDING) {
            throw BusinessException.conflict("只有待审核的报名才能通过");
        }
        int approvedCount = activityMapper.countApproved(activity.getId());
        if (approvedCount >= activity.getCapacity()) {
            throw BusinessException.conflict("名额已满，请先拒绝或等待其他报名取消");
        }

        registrationMapper.updateStatus(registrationId, RegistrationStatus.APPROVED, LocalDateTime.now());
        publishAfterCommit(registration, RegistrationEventType.APPROVED, RegistrationStatus.APPROVED);
    }

    @Transactional
    public void reject(Long registrationId) {
        Registration snapshot = registrationMapper.findById(registrationId);
        if (snapshot == null) {
            throw BusinessException.notFound("报名记录不存在");
        }
        activityMapper.findByIdForUpdate(snapshot.getActivityId());

        Registration registration = registrationMapper.findByIdForUpdate(registrationId);
        if (registration == null || registration.getStatus() != RegistrationStatus.PENDING) {
            throw BusinessException.conflict("只有待审核的报名才能拒绝");
        }

        registrationMapper.updateStatus(registrationId, RegistrationStatus.REJECTED, LocalDateTime.now());
        publishAfterCommit(registration, RegistrationEventType.REJECTED, RegistrationStatus.REJECTED);
    }

    public List<RegistrationView> myRegistrations(String studentId) {
        return registrationMapper.findByStudent(studentId);
    }

    public List<RegistrationView> registrationsForAdmin(RegistrationStatus status, Long activityId) {
        return registrationMapper.findForAdmin(status, activityId);
    }

    private void publishAfterCommit(Registration registration, RegistrationEventType type, RegistrationStatus status) {
        RegistrationEvent event = new RegistrationEvent(
                type,
                status,
                registration.getId(),
                registration.getStudentId(),
                registration.getActivityId(),
                LocalDateTime.now()
        );
        TransactionHooks.afterCommit(() -> eventPublisher.publish(event));
    }
}