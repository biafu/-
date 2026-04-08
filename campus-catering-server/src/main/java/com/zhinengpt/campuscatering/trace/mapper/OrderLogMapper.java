package com.zhinengpt.campuscatering.trace.mapper;

import com.zhinengpt.campuscatering.trace.entity.OrderLog;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderLogMapper {

    @Insert("""
            insert into order_log(order_id, order_status, action_type, operator_type, operator_id, content, created_at)
            values(#{orderId}, #{orderStatus}, #{actionType}, #{operatorType}, #{operatorId}, #{content}, now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderLog orderLog);

    @Select("""
            select id, order_id, order_status, action_type, operator_type, operator_id, content, created_at
            from order_log
            where order_id = #{orderId}
            order by id asc
            """)
    List<OrderLog> selectByOrderId(Long orderId);
}
