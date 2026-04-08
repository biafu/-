package com.zhinengpt.campuscatering.seckill.controller;

import com.zhinengpt.campuscatering.common.response.ApiResponse;
import com.zhinengpt.campuscatering.seckill.dto.SeckillActivityResponse;
import com.zhinengpt.campuscatering.seckill.dto.SeckillApplyRequest;
import com.zhinengpt.campuscatering.seckill.dto.SeckillApplyResponse;
import com.zhinengpt.campuscatering.seckill.dto.SeckillResultResponse;
import com.zhinengpt.campuscatering.seckill.service.SeckillService;
import com.zhinengpt.campuscatering.security.SecurityUtils;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/seckill")
@PreAuthorize("hasRole('STUDENT')")
public class SeckillController {

    private final SeckillService seckillService;

    public SeckillController(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    @GetMapping("/activity/list")
    public ApiResponse<List<SeckillActivityResponse>> activityList(@RequestParam Long storeId) {
        return ApiResponse.success(seckillService.listActivities(storeId));
    }

    @PostMapping("/apply")
    public ApiResponse<SeckillApplyResponse> apply(@Valid @RequestBody SeckillApplyRequest request) {
        return ApiResponse.success(seckillService.apply(SecurityUtils.getLoginUser().getUserId(), request));
    }

    @GetMapping("/result/{requestId}")
    public ApiResponse<SeckillResultResponse> result(@PathVariable String requestId) {
        return ApiResponse.success(seckillService.queryResult(requestId));
    }
}
