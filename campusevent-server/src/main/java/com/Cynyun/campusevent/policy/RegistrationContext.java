package com.Cynyun.campusevent.policy;

import com.Cynyun.campusevent.entity.Activity;
import com.Cynyun.campusevent.entity.Student;

/**
 * 报名策略的上下文：把策略需要的业务数据一次性传入，
 * 避免每新增一条准入规则就修改策略接口。
 */
public record RegistrationContext(Student student, Activity activity, int approvedCount) {
}
