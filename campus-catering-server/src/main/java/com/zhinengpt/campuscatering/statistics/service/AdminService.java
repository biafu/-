package com.zhinengpt.campuscatering.statistics.service;

import com.zhinengpt.campuscatering.auth.mapper.StudentUserMapper;
import com.zhinengpt.campuscatering.common.exception.BusinessException;
import com.zhinengpt.campuscatering.merchant.dto.MerchantAuditRequest;
import com.zhinengpt.campuscatering.merchant.entity.MerchantApply;
import com.zhinengpt.campuscatering.merchant.mapper.MerchantMapper;
import com.zhinengpt.campuscatering.merchant.service.MerchantService;
import com.zhinengpt.campuscatering.order.dto.DispatchRequest;
import com.zhinengpt.campuscatering.order.service.OrderService;
import com.zhinengpt.campuscatering.seckill.dto.SeckillActivityResponse;
import com.zhinengpt.campuscatering.seckill.dto.SeckillActivitySaveRequest;
import com.zhinengpt.campuscatering.seckill.service.SeckillService;
import com.zhinengpt.campuscatering.statistics.dto.DailyStatisticsResponse;
import com.zhinengpt.campuscatering.statistics.dto.DashboardOverviewResponse;
import com.zhinengpt.campuscatering.statistics.dto.MerchantRankResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final MerchantService merchantService;
    private final OrderService orderService;
    private final MerchantMapper merchantMapper;
    private final StudentUserMapper studentUserMapper;
    private final SeckillService seckillService;
    private final DailyStatisticsService dailyStatisticsService;

    public AdminService(MerchantService merchantService,
                        OrderService orderService,
                        MerchantMapper merchantMapper,
                        StudentUserMapper studentUserMapper,
                        SeckillService seckillService,
                        DailyStatisticsService dailyStatisticsService) {
        this.merchantService = merchantService;
        this.orderService = orderService;
        this.merchantMapper = merchantMapper;
        this.studentUserMapper = studentUserMapper;
        this.seckillService = seckillService;
        this.dailyStatisticsService = dailyStatisticsService;
    }

    public List<MerchantApply> listApplications() {
        return merchantService.listApplications();
    }

    public void auditMerchant(MerchantAuditRequest request) {
        merchantService.auditApply(request);
    }

    public void dispatch(DispatchRequest request) {
        orderService.dispatch(request);
    }

    public void completeDelivery(Long orderId) {
        orderService.completeDelivery(orderId);
    }

    public void enableStore(Long storeId) {
        merchantService.updateStoreBusinessStatus(storeId, 1);
    }

    public void disableStore(Long storeId) {
        merchantService.updateStoreBusinessStatus(storeId, 0);
    }

    public Long saveSeckillActivity(SeckillActivitySaveRequest request) {
        return seckillService.saveActivity(request);
    }

    public List<SeckillActivityResponse> listSeckillActivities() {
        return seckillService.listAllActivities();
    }

    public void deleteSeckillActivity(Long activityId) {
        seckillService.deleteActivity(activityId);
    }

    public List<DailyStatisticsResponse> listDailyStatistics(Integer days) {
        return dailyStatisticsService.latest(days == null ? 7 : days);
    }

    public void rebuildDailyStatistics(LocalDate statDate) {
        if (statDate == null) {
            throw new BusinessException("统计日期不能为空");
        }
        if (statDate.isAfter(LocalDate.now())) {
            throw new BusinessException("不能重算未来日期数据");
        }
        dailyStatisticsService.summarizeByDate(statDate);
    }

    public List<MerchantRankResponse> merchantRank(Integer days, Integer limit) {
        return orderService.merchantRank(days == null ? 7 : days, limit == null ? 10 : limit);
    }

    public DashboardOverviewResponse dashboard() {
        return DashboardOverviewResponse.builder()
                .orderCount(orderService.countPlatformOrders())
                .totalRevenue(orderService.sumPlatformRevenue())
                .merchantCount(merchantMapper.countActiveMerchants())
                .activeUserCount(studentUserMapper.countActiveUsers())
                .build();
    }
}
