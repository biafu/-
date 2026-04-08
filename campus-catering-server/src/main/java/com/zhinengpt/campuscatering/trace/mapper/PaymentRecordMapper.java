package com.zhinengpt.campuscatering.trace.mapper;

import com.zhinengpt.campuscatering.trace.entity.PaymentRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PaymentRecordMapper {

    @Insert("""
            insert into payment_record(order_id, order_no, pay_amount, pay_channel, pay_status, transaction_no, paid_at, created_at, updated_at)
            values(#{orderId}, #{orderNo}, #{payAmount}, #{payChannel}, #{payStatus}, #{transactionNo}, #{paidAt}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PaymentRecord paymentRecord);

    @Select("""
            select id, order_id, order_no, pay_amount, pay_channel, pay_status, transaction_no, paid_at, created_at, updated_at
            from payment_record
            where order_id = #{orderId}
            limit 1
            """)
    PaymentRecord selectByOrderId(Long orderId);

    @Update("""
            update payment_record
            set pay_status = #{payStatus},
                transaction_no = #{transactionNo},
                paid_at = #{paidAt},
                updated_at = now()
            where order_id = #{orderId}
            """)
    int updateStatus(@Param("orderId") Long orderId,
                     @Param("payStatus") String payStatus,
                     @Param("transactionNo") String transactionNo,
                     @Param("paidAt") java.time.LocalDateTime paidAt);
}
