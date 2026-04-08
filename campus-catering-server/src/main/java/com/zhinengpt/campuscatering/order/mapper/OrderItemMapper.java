package com.zhinengpt.campuscatering.order.mapper;

import com.zhinengpt.campuscatering.order.entity.OrderItem;
import com.zhinengpt.campuscatering.statistics.dto.MerchantHotProductResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderItemMapper {

    @Insert("""
            insert into order_item(order_id, sku_id, spu_name, sku_name, price, quantity, total_amount)
            values(#{orderId}, #{skuId}, #{spuName}, #{skuName}, #{price}, #{quantity}, #{totalAmount})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderItem orderItem);

    @Select("""
            select id, order_id, sku_id, spu_name, sku_name, price, quantity, total_amount
            from order_item
            where order_id = #{orderId}
            order by id asc
            """)
    List<OrderItem> selectByOrderId(Long orderId);

    @Select("""
            select oi.spu_name as spu_name,
                   oi.sku_name as sku_name,
                   sum(oi.quantity) as sold_quantity,
                   coalesce(sum(oi.total_amount), 0) as gmv
            from order_item oi
            join orders o on o.id = oi.order_id
            where o.merchant_id = #{merchantId}
              and o.created_at >= #{startTime}
              and o.order_status in ('PAID', 'ACCEPTED', 'PREPARING', 'WAITING_DELIVERY', 'DELIVERING', 'COMPLETED')
            group by oi.spu_name, oi.sku_name
            order by sold_quantity desc, gmv desc
            limit #{limit}
            """)
    List<MerchantHotProductResponse> selectMerchantHotProducts(@Param("merchantId") Long merchantId,
                                                               @Param("startTime") LocalDateTime startTime,
                                                               @Param("limit") Integer limit);
}
