package com.Cynyun.campusevent.policy;

import com.Cynyun.campusevent.enums.RegistrationStatus;
import org.springframework.stereotype.Component;

@Component
public class ApprovalRegistrationPolicy implements RegistrationPolicy {

    @Override
    public RegistrationStatus decide(int approvedCount, int capacity) {
        return RegistrationStatus.PENDING;
    }
}