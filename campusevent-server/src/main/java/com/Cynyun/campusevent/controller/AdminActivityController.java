package com.Cynyun.campusevent.controller;

import com.Cynyun.campusevent.common.ApiResponse;
import com.Cynyun.campusevent.dto.ActivityRequest;
import com.Cynyun.campusevent.entity.Activity;
import com.Cynyun.campusevent.entity.ActivityStats;
import com.Cynyun.campusevent.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminActivityController {

    private final ActivityService activityService;

    @GetMapping("/activities")
    public ApiResponse<List<Activity>> list(@RequestParam(defaultValue = "false") boolean includeDeleted) {
        return ApiResponse.ok(activityService.listForAdmin(includeDeleted));
    }

    @GetMapping("/activities/{id}")
    public ApiResponse<Activity> detail(@PathVariable Long id) {
        return ApiResponse.ok(activityService.getAny(id));
    }

    @PostMapping("/activities")
    public ApiResponse<Activity> create(@Valid @RequestBody ActivityRequest request) {
        return ApiResponse.ok(activityService.create(request));
    }

    @PutMapping("/activities/{id}")
    public ApiResponse<Activity> update(@PathVariable Long id,
                                        @Valid @RequestBody ActivityRequest request) {
        return ApiResponse.ok(activityService.update(id, request));
    }

    @DeleteMapping("/activities/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        activityService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/statistics")
    public ApiResponse<List<ActivityStats>> statistics() {
        return ApiResponse.ok(activityService.statistics());
    }
}