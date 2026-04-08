package com.zhinengpt.campuscatering.seckill.mapper;

import com.zhinengpt.campuscatering.seckill.entity.SeckillOrder;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillOrderMapper {

    @Select("""
            select id, activity_id, user_id, order_id, order_no, status, created_at, updated_at
            from seckill_order
            where activity_id = #{activityId}
              and user_id = #{userId}
            limit 1
            """)
    SeckillOrder selectByUserAndActivity(@Param("userId") Long userId, @Param("activityId") Long activityId);

    @Select("""
            select id, activity_id, user_id, order_id, order_no, status, created_at, updated_at
            from seckill_order
            where order_id = #{orderId}
            limit 1
            """)
    SeckillOrder selectByOrderId(Long orderId);

    @Select("""
            select count(1)
            from seckill_order
            where activity_id = #{activityId}
            """)
    Integer countByActivityId(Long activityId);

    @Insert("""
            insert into seckill_order(activity_id, user_id, order_id, order_no, status, created_at, updated_at)
            values(#{activityId}, #{userId}, #{orderId}, #{orderNo}, #{status}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SeckillOrder seckillOrder);

    @Update("""
            update seckill_order
            set status = #{status}, updated_at = now()
            where order_id = #{orderId}
            """)
    int updateStatusByOrderId(@Param("orderId") Long orderId, @Param("status") Integer status);

    @Delete("delete from seckill_order where order_id = #{orderId}")
    int deleteByOrderId(Long orderId);
}
