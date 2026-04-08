package com.zhinengpt.campuscatering.statistics.service;

import com.zhinengpt.campuscatering.order.mapper.OrderMapper;
import com.zhinengpt.campuscatering.statistics.dto.DailyStatisticsResponse;
import com.zhinengpt.campuscatering.statistics.entity.DailyStatistics;
import com.zhinengpt.campuscatering.statistics.mapper.DailyStatisticsMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DailyStatisticsService {

    private final OrderMapper orderMapper;
    private final DailyStatisticsMapper dailyStatisticsMapper;

    public DailyStatisticsService(OrderMapper orderMapper, DailyStatisticsMapper dailyStatisticsMapper) {
        this.orderMapper = orderMapper;
        this.dailyStatisticsMapper = dailyStatisticsMapper;
    }

    public void summarizeByDate(LocalDate statDate) {
        long orderCount = safeLong(orderMapper.countDailyOrders(statDate));
        long completedOrderCount = safeLong(orderMapper.countDailyCompletedOrders(statDate));
        long cancelledOrderCount = safeLong(orderMapper.countDailyCancelledOrders(statDate));
        BigDecimal gmv = safeDecimal(orderMapper.sumDailyGmv(statDate));
        long activeUserCount = safeLong(orderMapper.countDailyActiveUsers(statDate));
        long activeMerchantCount = safeLong(orderMapper.countDailyActiveMerchants(statDate));
        int avgFulfillmentMinutes = safeInt(orderMapper.avgDailyFulfillmentMinutes(statDate));

        BigDecimal cancelRate = BigDecimal.ZERO;
        if (orderCount > 0) {
            cancelRate = BigDecimal.valueOf(cancelledOrderCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP);
        }

        DailyStatistics dailyStatistics = new DailyStatistics();
        dailyStatistics.setStatDate(statDate);
        dailyStatistics.setOrderCount(orderCount);
        dailyStatistics.setCompletedOrderCount(completedOrderCount);
        dailyStatistics.setCancelledOrderCount(cancelledOrderCount);
        dailyStatistics.setGmv(gmv);
        dailyStatistics.setActiveUserCount(activeUserCount);
        dailyStatistics.setActiveMerchantCount(activeMerchantCount);
        dailyStatistics.setCancelRate(cancelRate);
        dailyStatistics.setAvgFulfillmentMinutes(avgFulfillmentMinutes);
        dailyStatisticsMapper.upsert(dailyStatistics);
    }

    public void summarizeYesterday() {
        summarizeByDate(LocalDate.now().minusDays(1));
    }

    public List<DailyStatisticsResponse> latest(int days) {
        int limit = Math.max(1, Math.min(days, 31));
        return dailyStatisticsMapper.selectLatest(limit).stream()
                .map(item -> DailyStatisticsResponse.builder()
                        .statDate(item.getStatDate())
                        .orderCount(item.getOrderCount())
                        .completedOrderCount(item.getCompletedOrderCount())
                        .cancelledOrderCount(item.getCancelledOrderCount())
                        .gmv(item.getGmv())
                        .activeUserCount(item.getActiveUserCount())
                        .activeMerchantCount(item.getActiveMerchantCount())
                        .cancelRate(item.getCancelRate())
                        .avgFulfillmentMinutes(item.getAvgFulfillmentMinutes())
                        .build())
                .toList();
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal safeDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
