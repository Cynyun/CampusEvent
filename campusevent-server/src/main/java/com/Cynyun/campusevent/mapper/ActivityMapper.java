package com.Cynyun.campusevent.mapper;

import com.Cynyun.campusevent.entity.Activity;
import com.Cynyun.campusevent.enums.PolicyType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ActivityMapper {

    List<Activity> findActive(@Param("keyword") String keyword,
                              @Param("policyType") PolicyType policyType,
                              @Param("category") String category);

    List<Activity> findAllForAdmin(@Param("includeDeleted") boolean includeDeleted);

    Activity findById(@Param("id") Long id);

    Activity findByIdForUpdate(@Param("id") Long id);

    int countApproved(@Param("activityId") Long activityId);

    int countPending(@Param("activityId") Long activityId);

    int insert(Activity activity);

    int update(Activity activity);

    int softDelete(@Param("id") Long id, @Param("updatedAt") LocalDateTime updatedAt);
}