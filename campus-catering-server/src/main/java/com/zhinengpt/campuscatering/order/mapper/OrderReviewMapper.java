package com.zhinengpt.campuscatering.order.mapper;

import com.zhinengpt.campuscatering.order.dto.MerchantReviewResponse;
import com.zhinengpt.campuscatering.order.entity.OrderReview;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderReviewMapper {

    @Insert("""
            insert into order_review(order_id, user_id, merchant_id, store_id, rating, content, is_anonymous, created_at, updated_at)
            values(#{orderId}, #{userId}, #{merchantId}, #{storeId}, #{rating}, #{content}, #{isAnonymous}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderReview orderReview);

    @Select("""
            select id, order_id, user_id, merchant_id, store_id, rating, content, is_anonymous, created_at, updated_at
            from order_review
            where order_id = #{orderId}
            limit 1
            """)
    OrderReview selectByOrderId(Long orderId);

    @Select("""
            select r.order_id as orderId,
                   o.order_no as orderNo,
                   r.store_id as storeId,
                   s.store_name as storeName,
                   r.rating as rating,
                   r.content as content,
                   r.is_anonymous as isAnonymous,
                   case
                       when r.is_anonymous = 1 then '匿名用户'
                       else coalesce(su.nickname, concat('用户', r.user_id))
                   end as reviewerName,
                   r.created_at as createdAt
            from order_review r
            left join orders o on o.id = r.order_id
            left join store s on s.id = r.store_id
            left join student_user su on su.id = r.user_id
            where r.merchant_id = #{merchantId}
            order by r.created_at desc, r.id desc
            """)
    List<MerchantReviewResponse> selectByMerchantId(Long merchantId);
}
