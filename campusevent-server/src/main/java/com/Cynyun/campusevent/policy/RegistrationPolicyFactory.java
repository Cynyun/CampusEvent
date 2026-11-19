package com.Cynyun.campusevent.policy;

import com.Cynyun.campusevent.common.BusinessException;
import com.Cynyun.campusevent.enums.PolicyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegistrationPolicyFactory {

    private final DirectRegistrationPolicy directPolicy;
    private final ApprovalRegistrationPolicy approvalPolicy;

    public RegistrationPolicy create(PolicyType type) {
        return switch (type) {
            case DIRECT -> directPolicy;
            case APPROVAL -> approvalPolicy;
        };
    }
}