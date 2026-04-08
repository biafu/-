package com.zhinengpt.campuscatering.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhinengpt.campuscatering.order.dto.MerchantReviewResponse;
import com.zhinengpt.campuscatering.order.entity.Order;
import com.zhinengpt.campuscatering.order.entity.OrderReview;
import com.zhinengpt.campuscatering.order.mapper.OrderMapper;
import com.zhinengpt.campuscatering.order.mapper.OrderReviewMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class OrderReviewServiceTest {

    @Autowired
    private OrderReviewService orderReviewService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderReviewMapper orderReviewMapper;

    @Test
    void listMerchantReviews_returnsMerchantVisibleReviewFeed() {
        Order order = new Order();
        order.setOrderNo(202604080099L);
        order.setUserId(1L);
        order.setStoreId(1L);
        order.setMerchantId(1L);
        order.setOrderType(1);
        order.setOrderStatus("COMPLETED");
        order.setGoodsAmount(new BigDecimal("18.00"));
        order.setDeliveryFee(new BigDecimal("2.00"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(new BigDecimal("20.00"));
        order.setReceiverName("张三");
        order.setReceiverPhone("13800000001");
        order.setReceiverAddress("图书馆门口");
        order.setCompleteTime(LocalDateTime.now());
        orderMapper.insert(order);

        OrderReview orderReview = new OrderReview();
        orderReview.setOrderId(order.getId());
        orderReview.setUserId(1L);
        orderReview.setMerchantId(1L);
        orderReview.setStoreId(1L);
        orderReview.setRating(5);
        orderReview.setContent("出餐很快");
        orderReview.setIsAnonymous(1);
        orderReviewMapper.insert(orderReview);

        List<MerchantReviewResponse> reviews = orderReviewService.listMerchantReviews(1L);

        assertNotNull(reviews);
        assertFalse(reviews.isEmpty());
        assertTrue(reviews.stream().anyMatch(item -> order.getId().equals(item.getOrderId())));

        MerchantReviewResponse review = reviews.stream()
                .filter(item -> order.getId().equals(item.getOrderId()))
                .findFirst()
                .orElseThrow();
        assertEquals(order.getOrderNo(), review.getOrderNo());
        assertEquals(5, review.getRating());
        assertEquals("匿名用户", review.getReviewerName());
        assertEquals("出餐很快", review.getContent());
        assertNotNull(review.getCreatedAt());
    }
}
