package com.Cynyun.campusevent.service;

import com.Cynyun.campusevent.common.BusinessException;
import com.Cynyun.campusevent.dto.ActivityRequest;
import com.Cynyun.campusevent.entity.Activity;
import com.Cynyun.campusevent.entity.ActivityStats;
import com.Cynyun.campusevent.entity.Registration;
import com.Cynyun.campusevent.enums.PolicyType;
import com.Cynyun.campusevent.mapper.ActivityMapper;
import com.Cynyun.campusevent.mapper.RegistrationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityMapper activityMapper;
    private final RegistrationMapper registrationMapper;

    public List<Activity> listActive(String keyword, PolicyType policyType, String category) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : "%" + keyword.trim() + "%";
        String normalizedCategory = category == null || category.isBlank() ? null : category.trim();
        return sortForDisplay(activityMapper.findActive(normalizedKeyword, policyType, normalizedCategory));
    }

    public Activity getForStudent(Long id, String studentId) {
        Activity activity = getActive(id);
        Registration registration = registrationMapper.findActiveByStudentAndActivity(studentId, id);
        activity.setMyRegistrationStatus(registration == null ? null : registration.getStatus().name());
        activity.setMyRegistrationId(registration == null ? null : registration.getId());
        return activity;
    }

    public Activity getAny(Long id) {
        Activity activity = activityMapper.findById(id);
        if (activity == null) {
            throw BusinessException.notFound("活动不存在");
        }
        return activity;
    }

    public List<Activity> listForAdmin(boolean includeDeleted) {
        return sortForDisplay(activityMapper.findAllForAdmin(includeDeleted));
    }

    @Transactional
    public Activity create(ActivityRequest request) {
        LocalDateTime now = LocalDateTime.now();
        if (!request.getStartAt().isAfter(now)) {
            throw BusinessException.badRequest("开始时间必须晚于当前时间");
        }

        Activity activity = new Activity();
        apply(activity, request);
        activity.setCreatedAt(now);
        activity.setUpdatedAt(now);
        activityMapper.insert(activity);
        return getAny(activity.getId());
    }

    @Transactional
    public Activity update(Long id, ActivityRequest request) {
        Activity existing = requireActive(id);
        int approvedCount = activityMapper.countApproved(id);
        if (request.getCapacity() < approvedCount) {
            throw BusinessException.badRequest("人数上限不能小于已通过人数（" + approvedCount + "）");
        }
        LocalDateTime now = LocalDateTime.now();
        if (!request.getStartAt().equals(existing.getStartAt()) && !request.getStartAt().isAfter(now)) {
            throw BusinessException.badRequest("新的开始时间必须晚于当前时间");
        }

        apply(existing, request);
        existing.setUpdatedAt(now);
        int rows = activityMapper.update(existing);
        if (rows == 0) {
            throw BusinessException.notFound("活动不存在或已删除");
        }
        return getAny(id);
    }

    @Transactional
    public void delete(Long id) {
        requireActive(id);
        int rows = activityMapper.softDelete(id, LocalDateTime.now());
        if (rows == 0) {
            throw BusinessException.notFound("活动不存在或已删除");
        }
    }

    public List<ActivityStats> statistics() {
        return sortForDisplay(activityMapper.findAllForAdmin(false)).stream().map(ActivityStats::from).toList();
    }

    private List<Activity> sortForDisplay(List<Activity> activities) {
        return activities.stream()
                .sorted(Comparator
                        .comparing((Activity activity) -> !activity.isOpen())
                        .thenComparing(Activity::getStartAt,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private Activity getActive(Long id) {
        Activity activity = activityMapper.findById(id);
        if (activity == null || activity.getDeletedAt() != null) {
            throw BusinessException.notFound("活动不存在或已删除");
        }
        return activity;
    }

    private Activity requireActive(Long id) {
        return getActive(id);
    }

    private void apply(Activity activity, ActivityRequest request) {
        activity.setTitle(request.getTitle().trim());
        activity.setCategory(request.getCategory().trim());
        activity.setStartAt(request.getStartAt());
        activity.setLocation(request.getLocation().trim());
        activity.setCapacity(request.getCapacity());
        activity.setPolicyType(request.getPolicyType());
    }
}