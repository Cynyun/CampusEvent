package com.Cynyun.campusevent.policy;

import com.Cynyun.campusevent.enums.PolicyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegistrationPolicyFactory {

    private final DirectRegistrationPolicy directPolicy;
    private final ApprovalRegistrationPolicy approvalPolicy;
    private final GradeLimitedDirectRegistrationPolicy gradeLimitedDirectPolicy;

    public RegistrationPolicy create(PolicyType type) {
        return switch (type) {
            case DIRECT -> directPolicy;
            case APPROVAL -> approvalPolicy;
            case GRADE_LIMITED_DIRECT -> gradeLimitedDirectPolicy;
        };
    }
}
