package com.Cynyun.campusevent.policy;

import com.Cynyun.campusevent.common.BusinessException;
import com.Cynyun.campusevent.enums.RegistrationStatus;
import org.springframework.stereotype.Component;

@Component
public class DirectRegistrationPolicy implements RegistrationPolicy {

    @Override
    public RegistrationStatus decide(RegistrationContext context) {
        if (context.approvedCount() >= context.activity().getCapacity()) {
            throw BusinessException.conflict("活动名额已满");
        }
        return RegistrationStatus.APPROVED;
    }
}
