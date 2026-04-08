package com.zhinengpt.campuscatering.order.mapper;

import com.zhinengpt.campuscatering.order.entity.Order;
import com.zhinengpt.campuscatering.statistics.dto.MerchantRankResponse;
import com.zhinengpt.campuscatering.statistics.dto.MerchantStatusDistributionResponse;
import com.zhinengpt.campuscatering.statistics.dto.MerchantTrendPointResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderMapper {

    @Insert("""
            insert into orders(order_no, user_id, store_id, merchant_id, order_type, order_status, goods_amount, delivery_fee, discount_amount, pay_amount,
                               receiver_name, receiver_phone, receiver_address, remark, pay_time, accept_time, complete_time, cancel_time, cancel_reason, created_at, updated_at)
            values(#{orderNo}, #{userId}, #{storeId}, #{merchantId}, #{orderType}, #{orderStatus}, #{goodsAmount}, #{deliveryFee}, #{discountAmount}, #{payAmount},
                   #{receiverName}, #{receiverPhone}, #{receiverAddress}, #{remark}, #{payTime}, #{acceptTime}, #{completeTime}, #{cancelTime}, #{cancelReason}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Select("""
            select id, order_no, user_id, store_id, merchant_id, order_type, order_status, goods_amount, delivery_fee, discount_amount, pay_amount,
                   receiver_name, receiver_phone, receiver_address, remark, pay_time, accept_time, complete_time, cancel_time, cancel_reason, created_at, updated_at
            from orders
            where id = #{id}
            limit 1
            """)
    Order selectById(Long id);

    @Select("""
            select id, order_no, user_id, store_id, merchant_id, order_type, order_status, goods_amount, delivery_fee, discount_amount, pay_amount,
                   receiver_name, receiver_phone, receiver_address, remark, pay_time, accept_time, complete_time, cancel_time, cancel_reason, created_at, updated_at
            from orders
            where user_id = #{userId}
            order by id desc
            """)
    List<Order> selectByUserId(Long userId);

    @Select("""
            select id, order_no, user_id, store_id, merchant_id, order_type, order_status, goods_amount, delivery_fee, discount_amount, pay_amount,
                   receiver_name, receiver_phone, receiver_address, remark, pay_time, accept_time, complete_time, cancel_time, cancel_reason, created_at, updated_at
            from orders
            where merchant_id = #{merchantId}
            order by id desc
            """)
    List<Order> selectByMerchantId(Long merchantId);

    @Select("""
            select id, order_no, user_id, store_id, merchant_id, order_type, order_status, goods_amount, delivery_fee, discount_amount, pay_amount,
                   receiver_name, receiver_phone, receiver_address, remark, pay_time, accept_time, complete_time, cancel_time, cancel_reason, created_at, updated_at
            from orders
            where order_status = 'PENDING_PAYMENT'
              and created_at <= #{cutoff}
            """)
    List<Order> selectTimeoutPendingOrders(@Param("cutoff") LocalDateTime cutoff);

    @Select("""
            select id, order_no, user_id, store_id, merchant_id, order_type, order_status, goods_amount, delivery_fee, discount_amount, pay_amount,
                   receiver_name, receiver_phone, receiver_address, remark, pay_time, accept_time, complete_time, cancel_time, cancel_reason, created_at, updated_at
            from orders
            where order_status = 'PAID'
              and pay_time <= #{cutoff}
            """)
    List<Order> selectTimeoutPaidOrders(@Param("cutoff") LocalDateTime cutoff);

    @Select("""
            select id, order_no, user_id, store_id, merchant_id, order_type, order_status, goods_amount, delivery_fee, discount_amount, pay_amount,
                   receiver_name, receiver_phone, receiver_address, remark, pay_time, accept_time, complete_time, cancel_time, cancel_reason, created_at, updated_at
            from orders
            where order_status = 'DELIVERING'
              and updated_at <= #{cutoff}
            """)
    List<Order> selectTimeoutDeliveringOrders(@Param("cutoff") LocalDateTime cutoff);

    @Update("""
            update orders
            set order_status = 'PAID', pay_time = now(), updated_at = now()
            where id = #{id} and order_status = 'PENDING_PAYMENT'
            """)
    int pay(@Param("id") Long id);

    @Update("""
            update orders
            set order_status = 'ACCEPTED', accept_time = now(), updated_at = now()
            where id = #{id} and order_status = 'PAID'
            """)
    int accept(@Param("id") Long id);

    @Update("""
            update orders
            set order_status = 'PREPARING', updated_at = now()
            where id = #{id} and order_status = 'ACCEPTED'
            """)
    int prepare(@Param("id") Long id);

    @Update("""
            update orders
            set order_status = 'WAITING_DELIVERY', updated_at = now()
            where id = #{id} and order_status = 'PREPARING'
            """)
    int finishPrepare(@Param("id") Long id);

    @Update("""
            update orders
            set order_status = 'DELIVERING', updated_at = now()
            where id = #{id} and order_status = 'WAITING_DELIVERY'
            """)
    int delivering(@Param("id") Long id);

    @Update("""
            update orders
            set order_status = 'COMPLETED', complete_time = now(), updated_at = now()
            where id = #{id} and order_status = 'DELIVERING'
            """)
    int complete(@Param("id") Long id);

    @Update("""
            update orders
            set order_status = 'CANCELLED', cancel_time = now(), cancel_reason = #{reason}, updated_at = now()
            where id = #{id} and order_status = #{expectedStatus}
            """)
    int cancel(@Param("id") Long id, @Param("expectedStatus") String expectedStatus, @Param("reason") String reason);

    @Update("""
            update orders
            set order_status = 'REFUNDED', cancel_time = now(), cancel_reason = #{reason}, updated_at = now()
            where id = #{id}
              and order_status in ('PAID', 'ACCEPTED', 'PREPARING', 'WAITING_DELIVERY')
            """)
    int refund(@Param("id") Long id, @Param("reason") String reason);

    @Select("select count(1) from orders where merchant_id = #{merchantId}")
    Long countMerchantTotalOrders(Long merchantId);

    @Select("""
            select count(1)
            from orders
            where merchant_id = #{merchantId}
              and date(created_at) = current_date()
            """)
    Long countMerchantTodayOrders(Long merchantId);

    @Select("""
            select coalesce(sum(pay_amount), 0)
            from orders
            where merchant_id = #{merchantId}
              and order_status in ('PAID', 'ACCEPTED', 'PREPARING', 'WAITING_DELIVERY', 'DELIVERING', 'COMPLETED')
              and date(created_at) = current_date()
            """)
    java.math.BigDecimal sumMerchantTodayRevenue(Long merchantId);

    @Select("select count(1) from orders")
    Long countPlatformOrders();

    @Select("""
            select coalesce(sum(pay_amount), 0)
            from orders
            where order_status in ('PAID', 'ACCEPTED', 'PREPARING', 'WAITING_DELIVERY', 'DELIVERING', 'COMPLETED')
            """)
    java.math.BigDecimal sumPlatformRevenue();

    @Select("""
            select count(1)
            from orders
            where date(created_at) = #{statDate}
            """)
    Long countDailyOrders(@Param("statDate") LocalDate statDate);

    @Select("""
            select count(1)
            from orders
            where order_status = 'COMPLETED'
              and date(created_at) = #{statDate}
            """)
    Long countDailyCompletedOrders(@Param("statDate") LocalDate statDate);

    @Select("""
            select count(1)
            from orders
            where order_status = 'CANCELLED'
              and date(created_at) = #{statDate}
            """)
    Long countDailyCancelledOrders(@Param("statDate") LocalDate statDate);

    @Select("""
            select coalesce(sum(pay_amount), 0)
            from orders
            where order_status in ('PAID', 'ACCEPTED', 'PREPARING', 'WAITING_DELIVERY', 'DELIVERING', 'COMPLETED')
              and date(created_at) = #{statDate}
            """)
    java.math.BigDecimal sumDailyGmv(@Param("statDate") LocalDate statDate);

    @Select("""
            select count(distinct user_id)
            from orders
            where date(created_at) = #{statDate}
            """)
    Long countDailyActiveUsers(@Param("statDate") LocalDate statDate);

    @Select("""
            select count(distinct merchant_id)
            from orders
            where date(created_at) = #{statDate}
            """)
    Long countDailyActiveMerchants(@Param("statDate") LocalDate statDate);

    @Select("""
            select coalesce(round(avg(timestampdiff(minute, pay_time, complete_time))), 0)
            from orders
            where order_status = 'COMPLETED'
              and pay_time is not null
              and complete_time is not null
              and date(created_at) = #{statDate}
            """)
    Integer avgDailyFulfillmentMinutes(@Param("statDate") LocalDate statDate);

    @Select("""
            select o.merchant_id as merchantId,
                   max(m.merchant_name) as merchantName,
                   count(1) as orderCount,
                   coalesce(sum(o.pay_amount), 0) as gmv
            from orders o
            left join merchant m on m.id = o.merchant_id
            where o.created_at >= #{startTime}
              and o.order_status in ('PAID', 'ACCEPTED', 'PREPARING', 'WAITING_DELIVERY', 'DELIVERING', 'COMPLETED')
            group by o.merchant_id
            order by gmv desc, orderCount desc
            limit #{limit}
            """)
    List<MerchantRankResponse> selectMerchantRank(@Param("startTime") LocalDateTime startTime, @Param("limit") Integer limit);

    @Select("""
            select date(o.created_at) as stat_date,
                   count(1) as order_count,
                   coalesce(sum(case when o.order_status in ('PAID', 'ACCEPTED', 'PREPARING', 'WAITING_DELIVERY', 'DELIVERING', 'COMPLETED')
                                     then o.pay_amount else 0 end), 0) as revenue,
                   coalesce(round(sum(case when o.order_status = 'CANCELLED' then 1 else 0 end) / count(1) * 100, 2), 0) as cancel_rate
            from orders o
            where o.merchant_id = #{merchantId}
              and date(o.created_at) >= #{startDate}
            group by date(o.created_at)
            order by stat_date asc
            """)
    List<MerchantTrendPointResponse> selectMerchantTrend(@Param("merchantId") Long merchantId, @Param("startDate") LocalDate startDate);

    @Select("""
            select o.order_status as order_status,
                   count(1) as order_count
            from orders o
            where o.merchant_id = #{merchantId}
              and o.created_at >= #{startTime}
            group by o.order_status
            order by order_count desc
            """)
    List<MerchantStatusDistributionResponse> selectMerchantStatusDistribution(@Param("merchantId") Long merchantId, @Param("startTime") LocalDateTime startTime);
}
