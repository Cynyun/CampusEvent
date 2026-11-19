package com.Cynyun.campusevent.dto;

import com.Cynyun.campusevent.enums.PolicyType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityRequest {

    @NotBlank(message = "活动名称不能为空")
    @Size(max = 100, message = "活动名称不能超过 100 个字符")
    private String title;

    @NotBlank(message = "活动分类不能为空")
    @Size(max = 50, message = "活动分类不能超过 50 个字符")
    private String category;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startAt;

    @NotBlank(message = "活动地点不能为空")
    @Size(max = 100, message = "活动地点不能超过 100 个字符")
    private String location;

    @NotNull(message = "人数上限不能为空")
    @Min(value = 1, message = "人数上限必须为正整数")
    private Integer capacity;

    @NotNull(message = "报名方式不能为空")
    private PolicyType policyType;
}