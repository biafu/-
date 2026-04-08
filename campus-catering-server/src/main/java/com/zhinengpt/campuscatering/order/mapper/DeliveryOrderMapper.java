package com.zhinengpt.campuscatering.order.mapper;

import com.zhinengpt.campuscatering.order.entity.DeliveryOrder;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DeliveryOrderMapper {

    @Insert("""
            insert into delivery_order(order_id, delivery_user_id, dispatch_status, pickup_time, delivered_time, dispatch_remark, created_at, updated_at)
            values(#{orderId}, #{deliveryUserId}, #{dispatchStatus}, #{pickupTime}, #{deliveredTime}, #{dispatchRemark}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DeliveryOrder deliveryOrder);

    @Select("""
            select id, order_id, delivery_user_id, dispatch_status, pickup_time, delivered_time, dispatch_remark, created_at, updated_at
            from delivery_order
            where order_id = #{orderId}
            limit 1
            """)
    DeliveryOrder selectByOrderId(Long orderId);

    @Select("""
            select id, order_id, delivery_user_id, dispatch_status, pickup_time, delivered_time, dispatch_remark, created_at, updated_at
            from delivery_order
            where delivery_user_id = #{deliveryUserId}
            order by created_at desc, id desc
            """)
    List<DeliveryOrder> selectByDeliveryUserId(Long deliveryUserId);

    @Select("""
            select id, order_id, delivery_user_id, dispatch_status, pickup_time, delivered_time, dispatch_remark, created_at, updated_at
            from delivery_order
            where dispatch_status = 0
            order by created_at desc, id desc
            """)
    List<DeliveryOrder> selectAvailableOrders();

    @Update("""
            update delivery_order
            set delivery_user_id = #{deliveryUserId},
                dispatch_status = 1,
                updated_at = now()
            where order_id = #{orderId}
              and dispatch_status = 0
            """)
    int claim(@Param("orderId") Long orderId, @Param("deliveryUserId") Long deliveryUserId);

    @Update("""
            update delivery_order
            set delivery_user_id = #{deliveryUserId},
                dispatch_status = 1,
                dispatch_remark = #{dispatchRemark},
                updated_at = now()
            where order_id = #{orderId}
              and dispatch_status = 0
            """)
    int assign(@Param("orderId") Long orderId,
               @Param("deliveryUserId") Long deliveryUserId,
               @Param("dispatchRemark") String dispatchRemark);

    @Update("""
            update delivery_order
            set dispatch_status = 2, pickup_time = now(), updated_at = now()
            where order_id = #{orderId}
              and delivery_user_id = #{deliveryUserId}
              and dispatch_status = 1
            """)
    int pickup(@Param("orderId") Long orderId, @Param("deliveryUserId") Long deliveryUserId);

    @Update("""
            update delivery_order
            set dispatch_status = 3, delivered_time = now(), updated_at = now()
            where order_id = #{orderId}
            """)
    int complete(Long orderId);

    @Update("""
            update delivery_order
            set dispatch_status = 3, delivered_time = now(), updated_at = now()
            where order_id = #{orderId}
              and delivery_user_id = #{deliveryUserId}
              and dispatch_status in (1, 2)
            """)
    int completeByDeliveryUser(@Param("orderId") Long orderId, @Param("deliveryUserId") Long deliveryUserId);
}
