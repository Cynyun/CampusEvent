package com.Cynyun.campusevent.policy;

import com.Cynyun.campusevent.common.BusinessException;
import com.Cynyun.campusevent.enums.RegistrationStatus;
import org.springframework.stereotype.Component;

/**
 * 年级限定的直接报名：仅大二（2）、大三（3）可报名。
 * 年级数据异常时先拒绝，再检查名额，保证错误文案稳定。
 */
@Component
public class GradeLimitedDirectRegistrationPolicy implements RegistrationPolicy {

    @Override
    public RegistrationStatus decide(RegistrationContext context) {
        Integer grade = context.student().getGrade();
        if (grade == null || grade < 1 || grade > 4) {
            throw BusinessException.forbidden("年级信息异常，请联系管理员");
        }
        if (grade != 2 && grade != 3) {
            throw BusinessException.forbidden("该活动仅限大二、大三学生报名");
        }
        if (context.approvedCount() >= context.activity().getCapacity()) {
            throw BusinessException.conflict("活动名额已满");
        }
        return RegistrationStatus.APPROVED;
    }
}
