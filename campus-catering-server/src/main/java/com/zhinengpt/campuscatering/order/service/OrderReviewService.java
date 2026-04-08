package com.zhinengpt.campuscatering.order.service;

import com.zhinengpt.campuscatering.common.enums.OrderStatus;
import com.zhinengpt.campuscatering.common.exception.BusinessException;
import com.zhinengpt.campuscatering.message.service.StudentMessageService;
import com.zhinengpt.campuscatering.order.dto.MerchantReviewResponse;
import com.zhinengpt.campuscatering.order.dto.OrderReviewRequest;
import com.zhinengpt.campuscatering.order.dto.OrderReviewResponse;
import com.zhinengpt.campuscatering.order.entity.Order;
import com.zhinengpt.campuscatering.order.entity.OrderReview;
import com.zhinengpt.campuscatering.order.mapper.OrderMapper;
import com.zhinengpt.campuscatering.order.mapper.OrderReviewMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OrderReviewService {

    private final OrderMapper orderMapper;
    private final OrderReviewMapper orderReviewMapper;
    private final StudentMessageService studentMessageService;

    public OrderReviewService(OrderMapper orderMapper,
                              OrderReviewMapper orderReviewMapper,
                              StudentMessageService studentMessageService) {
        this.orderMapper = orderMapper;
        this.orderReviewMapper = orderReviewMapper;
        this.studentMessageService = studentMessageService;
    }

    @Transactional(rollbackFor = Exception.class)
    public void review(Long userId, OrderReviewRequest request) {
        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BusinessException("订单不存在或无权限评价");
        }
        if (!OrderStatus.COMPLETED.name().equals(order.getOrderStatus())) {
            throw new BusinessException("仅已完成订单可评价");
        }
        if (orderReviewMapper.selectByOrderId(order.getId()) != null) {
            throw new BusinessException("该订单已评价，请勿重复提交");
        }

        OrderReview orderReview = new OrderReview();
        orderReview.setOrderId(order.getId());
        orderReview.setUserId(userId);
        orderReview.setMerchantId(order.getMerchantId());
        orderReview.setStoreId(order.getStoreId());
        orderReview.setRating(request.getRating());
        orderReview.setContent(trimToNull(request.getContent()));
        orderReview.setIsAnonymous(request.getIsAnonymous() != null && request.getIsAnonymous() == 1 ? 1 : 0);
        orderReviewMapper.insert(orderReview);
        studentMessageService.push(userId, "评价提交成功", "感谢你的反馈，商家将持续优化服务", "SYSTEM", "ORDER_REVIEW", order.getId());
    }

    public OrderReviewResponse detail(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BusinessException("订单不存在或无权限查看评价");
        }
        OrderReview review = orderReviewMapper.selectByOrderId(orderId);
        if (review == null) {
            return null;
        }
        return OrderReviewResponse.builder()
                .orderId(review.getOrderId())
                .rating(review.getRating())
                .content(review.getContent())
                .isAnonymous(review.getIsAnonymous())
                .createdAt(review.getCreatedAt())
                .build();
    }

    public List<MerchantReviewResponse> listMerchantReviews(Long merchantId) {
        return orderReviewMapper.selectByMerchantId(merchantId);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
