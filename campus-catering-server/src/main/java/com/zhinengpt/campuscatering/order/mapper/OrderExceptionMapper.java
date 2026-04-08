package com.zhinengpt.campuscatering.order.mapper;

import com.zhinengpt.campuscatering.order.dto.OrderExceptionResponse;
import com.zhinengpt.campuscatering.order.entity.OrderException;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderExceptionMapper {

    @Insert("""
            insert into order_exception(order_id, merchant_id, status, reason, resolved_remark, created_at, updated_at)
            values(#{orderId}, #{merchantId}, #{status}, #{reason}, #{resolvedRemark}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderException orderException);

    @Select("""
            select id, order_id, merchant_id, status, reason, resolved_remark, created_at, updated_at
            from order_exception
            where order_id = #{orderId}
            limit 1
            """)
    OrderException selectByOrderId(Long orderId);

    @Update("""
            update order_exception
            set status = #{status},
                reason = #{reason},
                resolved_remark = #{resolvedRemark},
                updated_at = now()
            where order_id = #{orderId}
            """)
    int updateByOrderId(OrderException orderException);

    @Update("""
            update order_exception
            set status = 'RESOLVED',
                resolved_remark = #{resolvedRemark},
                updated_at = now()
            where order_id = #{orderId} and merchant_id = #{merchantId}
            """)
    int resolve(@Param("orderId") Long orderId, @Param("merchantId") Long merchantId, @Param("resolvedRemark") String resolvedRemark);

    @Select("""
            select e.id as id,
                   e.order_id as order_id,
                   o.order_no as order_no,
                   o.order_status as order_status,
                   o.pay_amount as pay_amount,
                   e.reason as reason,
                   e.status as status,
                   e.resolved_remark as resolved_remark,
                   e.created_at as created_at,
                   e.updated_at as updated_at
            from order_exception e
            join orders o on o.id = e.order_id
            where e.merchant_id = #{merchantId}
              and (#{status} is null or #{status} = '' or e.status = #{status})
            order by e.id desc
            """)
    List<OrderExceptionResponse> selectByMerchant(@Param("merchantId") Long merchantId, @Param("status") String status);
}
