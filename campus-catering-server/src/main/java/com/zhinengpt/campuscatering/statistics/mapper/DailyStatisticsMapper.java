package com.zhinengpt.campuscatering.statistics.mapper;

import com.zhinengpt.campuscatering.statistics.entity.DailyStatistics;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DailyStatisticsMapper {

    @Insert("""
            insert into daily_statistics(stat_date, order_count, completed_order_count, cancelled_order_count, gmv,
                                         active_user_count, active_merchant_count, cancel_rate, avg_fulfillment_minutes, created_at, updated_at)
            values(#{statDate}, #{orderCount}, #{completedOrderCount}, #{cancelledOrderCount}, #{gmv},
                   #{activeUserCount}, #{activeMerchantCount}, #{cancelRate}, #{avgFulfillmentMinutes}, now(), now())
            on duplicate key update
                order_count = values(order_count),
                completed_order_count = values(completed_order_count),
                cancelled_order_count = values(cancelled_order_count),
                gmv = values(gmv),
                active_user_count = values(active_user_count),
                active_merchant_count = values(active_merchant_count),
                cancel_rate = values(cancel_rate),
                avg_fulfillment_minutes = values(avg_fulfillment_minutes),
                updated_at = now()
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int upsert(DailyStatistics dailyStatistics);

    @Select("""
            select id, stat_date, order_count, completed_order_count, cancelled_order_count, gmv,
                   active_user_count, active_merchant_count, cancel_rate, avg_fulfillment_minutes, created_at, updated_at
            from daily_statistics
            order by stat_date desc
            limit #{limit}
            """)
    List<DailyStatistics> selectLatest(@Param("limit") Integer limit);
}
