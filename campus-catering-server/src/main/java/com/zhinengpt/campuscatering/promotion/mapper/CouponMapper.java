package com.zhinengpt.campuscatering.promotion.mapper;

import com.zhinengpt.campuscatering.promotion.dto.CouponCenterResponse;
import com.zhinengpt.campuscatering.promotion.entity.Coupon;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CouponMapper {

    @Select("""
            select id, coupon_name, coupon_type, discount_amount, threshold_amount, store_id, start_time, end_time,
                   total_count, receive_count, status, created_at, updated_at
            from coupon
            where id = #{id}
            limit 1
            """)
    Coupon selectById(Long id);

    @Update("""
            update coupon
            set receive_count = receive_count + 1,
                updated_at = now()
            where id = #{couponId}
              and status = 1
              and start_time <= now()
              and end_time >= now()
              and receive_count < total_count
            """)
    int increaseReceiveCount(@Param("couponId") Long couponId);

    @Update("""
            update coupon
            set receive_count = greatest(receive_count - 1, 0),
                updated_at = now()
            where id = #{couponId}
            """)
    int decreaseReceiveCount(@Param("couponId") Long couponId);

    @Select("""
            select c.id as couponId,
                   c.coupon_name as couponName,
                   c.discount_amount as discountAmount,
                   c.threshold_amount as thresholdAmount,
                   c.store_id as storeId,
                   c.start_time as startTime,
                   c.end_time as endTime,
                   c.total_count as totalCount,
                   c.receive_count as receiveCount,
                   greatest(c.total_count - c.receive_count, 0) as remainingCount
            from coupon c
            where c.status = 1
              and c.start_time <= #{now}
              and c.end_time >= #{now}
              and c.receive_count < c.total_count
              and (#{storeId} is null or c.store_id is null or c.store_id = #{storeId})
              and not exists (
                select 1
                from user_coupon uc
                where uc.user_id = #{userId}
                  and uc.coupon_id = c.id
              )
            order by c.id desc
            """)
    List<CouponCenterResponse> selectClaimableCoupons(@Param("userId") Long userId,
                                                      @Param("storeId") Long storeId,
                                                      @Param("now") LocalDateTime now);
}
