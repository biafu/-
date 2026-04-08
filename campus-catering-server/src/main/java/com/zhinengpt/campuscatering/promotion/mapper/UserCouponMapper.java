package com.zhinengpt.campuscatering.promotion.mapper;

import com.zhinengpt.campuscatering.promotion.dto.MyCouponResponse;
import com.zhinengpt.campuscatering.promotion.entity.UserCoupon;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserCouponMapper {

    @Insert("""
            insert into user_coupon(user_id, coupon_id, status, used_order_id, used_at, created_at, updated_at)
            values(#{userId}, #{couponId}, #{status}, #{usedOrderId}, #{usedAt}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserCoupon userCoupon);

    @Select("""
            select id, user_id, coupon_id, status, used_order_id, used_at, created_at, updated_at
            from user_coupon
            where id = #{id}
            limit 1
            """)
    UserCoupon selectById(Long id);

    @Select("""
            select uc.id as userCouponId,
                   c.id as couponId,
                   c.coupon_name as couponName,
                   c.discount_amount as discountAmount,
                   c.threshold_amount as thresholdAmount,
                   c.store_id as storeId,
                   c.start_time as startTime,
                   c.end_time as endTime,
                   uc.status as status
            from user_coupon uc
            join coupon c on c.id = uc.coupon_id
            where uc.user_id = #{userId}
              and uc.status = 0
              and c.status = 1
              and c.start_time <= #{now}
              and c.end_time >= #{now}
              and (#{storeId} is null or c.store_id is null or c.store_id = #{storeId})
            order by uc.id desc
            """)
    List<MyCouponResponse> selectUsableByUser(@Param("userId") Long userId,
                                              @Param("storeId") Long storeId,
                                              @Param("now") LocalDateTime now);

    @Update("""
            update user_coupon
            set status = 1,
                used_order_id = #{orderId},
                used_at = now(),
                updated_at = now()
            where id = #{userCouponId}
              and user_id = #{userId}
              and status = 0
            """)
    int markUsed(@Param("userCouponId") Long userCouponId, @Param("userId") Long userId, @Param("orderId") Long orderId);

    @Update("""
            update user_coupon
            set status = 0,
                used_order_id = null,
                used_at = null,
                updated_at = now()
            where used_order_id = #{orderId}
              and status = 1
            """)
    int releaseByOrderId(@Param("orderId") Long orderId);
}
