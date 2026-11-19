package com.Cynyun.campusevent.policy;

import com.Cynyun.campusevent.common.BusinessException;
import com.Cynyun.campusevent.enums.RegistrationStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistrationPolicyTest {

    @Test
    void directPolicyApprovesWhenCapacityAvailable() {
        assertEquals(RegistrationStatus.APPROVED, new DirectRegistrationPolicy().decide(0, 10));
    }

    @Test
    void directPolicyRejectsWhenFull() {
        assertThrows(BusinessException.class, () -> new DirectRegistrationPolicy().decide(10, 10));
    }

    @Test
    void approvalPolicyAlwaysReturnsPendingEvenWhenFull() {
        assertEquals(RegistrationStatus.PENDING, new ApprovalRegistrationPolicy().decide(10, 10));
    }
}