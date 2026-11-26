package com.Cynyun.campusevent.policy;

import com.Cynyun.campusevent.common.BusinessException;
import com.Cynyun.campusevent.entity.Activity;
import com.Cynyun.campusevent.entity.Student;
import com.Cynyun.campusevent.enums.RegistrationStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistrationPolicyTest {

    @Test
    void directPolicyApprovesWhenCapacityAvailable() {
        assertEquals(RegistrationStatus.APPROVED,
                new DirectRegistrationPolicy().decide(context(null, 0, 10)));
    }

    @Test
    void directPolicyRejectsWhenFull() {
        assertThrows(BusinessException.class,
                () -> new DirectRegistrationPolicy().decide(context(null, 10, 10)));
    }

    @Test
    void approvalPolicyAlwaysReturnsPendingEvenWhenFull() {
        assertEquals(RegistrationStatus.PENDING,
                new ApprovalRegistrationPolicy().decide(context(null, 10, 10)));
    }

    @Test
    void gradeLimitedPolicyApprovesGradeTwo() {
        assertEquals(RegistrationStatus.APPROVED,
                new GradeLimitedDirectRegistrationPolicy().decide(context(2, 0, 10)));
    }

    @Test
    void gradeLimitedPolicyApprovesGradeThree() {
        assertEquals(RegistrationStatus.APPROVED,
                new GradeLimitedDirectRegistrationPolicy().decide(context(3, 9, 10)));
    }

    @Test
    void gradeLimitedPolicyRejectsGradeOne() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new GradeLimitedDirectRegistrationPolicy().decide(context(1, 0, 10)));
        assertEquals(403, ex.getStatus());
        assertEquals("该活动仅限大二、大三学生报名", ex.getMessage());
    }

    @Test
    void gradeLimitedPolicyRejectsGradeFour() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new GradeLimitedDirectRegistrationPolicy().decide(context(4, 0, 10)));
        assertEquals(403, ex.getStatus());
    }

    @Test
    void gradeLimitedPolicyRejectsMissingGrade() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new GradeLimitedDirectRegistrationPolicy().decide(context(null, 0, 10)));
        assertEquals(403, ex.getStatus());
    }

    @Test
    void gradeLimitedPolicyRejectsWhenFull() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new GradeLimitedDirectRegistrationPolicy().decide(context(2, 10, 10)));
        assertEquals(409, ex.getStatus());
    }

    private RegistrationContext context(Integer grade, int approvedCount, int capacity) {
        Student student = new Student();
        student.setStudentId("student");
        student.setGrade(grade);
        Activity activity = new Activity();
        activity.setId(1L);
        activity.setCapacity(capacity);
        return new RegistrationContext(student, activity, approvedCount);
    }
}
