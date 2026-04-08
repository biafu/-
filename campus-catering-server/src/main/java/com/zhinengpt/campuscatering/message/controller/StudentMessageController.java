package com.zhinengpt.campuscatering.message.controller;

import com.zhinengpt.campuscatering.common.response.ApiResponse;
import com.zhinengpt.campuscatering.message.dto.StudentMessageResponse;
import com.zhinengpt.campuscatering.message.service.StudentMessageService;
import com.zhinengpt.campuscatering.security.SecurityUtils;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/message")
@PreAuthorize("hasRole('STUDENT')")
public class StudentMessageController {

    private final StudentMessageService studentMessageService;

    public StudentMessageController(StudentMessageService studentMessageService) {
        this.studentMessageService = studentMessageService;
    }

    @GetMapping("/list")
    public ApiResponse<List<StudentMessageResponse>> list(@RequestParam(required = false) Boolean onlyUnread,
                                                          @RequestParam(required = false) Integer limit) {
        return ApiResponse.success(studentMessageService.list(
                SecurityUtils.getLoginUser().getUserId(),
                Boolean.TRUE.equals(onlyUnread),
                limit
        ));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount() {
        return ApiResponse.success(studentMessageService.unreadCount(SecurityUtils.getLoginUser().getUserId()));
    }

    @PostMapping("/read/{id}")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        studentMessageService.markRead(SecurityUtils.getLoginUser().getUserId(), id);
        return ApiResponse.success();
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        studentMessageService.markAllRead(SecurityUtils.getLoginUser().getUserId());
        return ApiResponse.success();
    }
}
