package com.Cynyun.campusevent.policy;

import com.Cynyun.campusevent.common.BusinessException;
import com.Cynyun.campusevent.enums.RegistrationStatus;
import org.springframework.stereotype.Component;

@Component
public class DirectRegistrationPolicy implements RegistrationPolicy {

    @Override
    public RegistrationStatus decide(int approvedCount, int capacity) {
        if (approvedCount >= capacity) {
            throw BusinessException.conflict("活动名额已满");
        }
        return RegistrationStatus.APPROVED;
    }
}