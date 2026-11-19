package com.Cynyun.campusevent.policy;

import com.Cynyun.campusevent.enums.RegistrationStatus;

public interface RegistrationPolicy {
    RegistrationStatus decide(int approvedCount, int capacity);
}