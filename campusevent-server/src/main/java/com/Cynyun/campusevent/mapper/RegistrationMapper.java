package com.Cynyun.campusevent.mapper;

import com.Cynyun.campusevent.entity.Registration;
import com.Cynyun.campusevent.entity.RegistrationView;
import com.Cynyun.campusevent.enums.RegistrationStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RegistrationMapper {

    Registration findActiveByStudentAndActivity(@Param("studentId") String studentId,
                                                @Param("activityId") Long activityId);

    Registration findById(@Param("id") Long id);

    Registration findByIdForUpdate(@Param("id") Long id);

    List<RegistrationView> findByStudent(@Param("studentId") String studentId);

    List<RegistrationView> findForAdmin(@Param("status") RegistrationStatus status,
                                        @Param("activityId") Long activityId);

    int insert(Registration registration);

    int updateStatus(@Param("id") Long id,
                     @Param("status") RegistrationStatus status,
                     @Param("updatedAt") LocalDateTime updatedAt);
}