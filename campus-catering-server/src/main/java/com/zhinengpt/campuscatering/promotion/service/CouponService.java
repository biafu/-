package com.zhinengpt.campuscatering.promotion.service;

import com.zhinengpt.campuscatering.common.exception.BusinessException;
import com.zhinengpt.campuscatering.promotion.dto.CouponCenterResponse;
import com.zhinengpt.campuscatering.promotion.dto.MyCouponResponse;
import com.zhinengpt.campuscatering.promotion.entity.Coupon;
import com.zhinengpt.campuscatering.promotion.entity.UserCoupon;
import com.zhinengpt.campuscatering.promotion.mapper.CouponMapper;
import com.zhinengpt.campuscatering.promotion.mapper.UserCouponMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    public CouponService(CouponMapper couponMapper, UserCouponMapper userCouponMapper) {
        this.couponMapper = couponMapper;
        this.userCouponMapper = userCouponMapper;
    }

    public List<CouponCenterResponse> claimableCoupons(Long userId, Long storeId) {
        return couponMapper.selectClaimableCoupons(userId, storeId, LocalDateTime.now());
    }

    public List<MyCouponResponse> usableCoupons(Long userId, Long storeId) {
        return userCouponMapper.selectUsableByUser(userId, storeId, LocalDateTime.now());
    }

    @Transactional(rollbackFor = Exception.class)
    public void claim(Long userId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        assertClaimable(coupon, LocalDateTime.now());

        if (couponMapper.increaseReceiveCount(couponId) == 0) {
            throw new BusinessException("优惠券已领完或活动已结束");
        }

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(0);
        try {
            userCouponMapper.insert(userCoupon);
        } catch (DuplicateKeyException ex) {
            couponMapper.decreaseReceiveCount(couponId);
            throw new BusinessException("该优惠券已领取");
        }
    }

    public BigDecimal calculateDiscount(Long userId,
                                        Long userCouponId,
                                        Long storeId,
                                        BigDecimal goodsAmount,
                                        BigDecimal orderAmount) {
        if (userCouponId == null) {
            return BigDecimal.ZERO;
        }
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
            throw new BusinessException("优惠券不存在");
        }
        if (userCoupon.getStatus() == null || userCoupon.getStatus() != 0) {
            throw new BusinessException("优惠券已被使用或失效");
        }

        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        assertUsable(coupon, storeId, goodsAmount, LocalDateTime.now());

        BigDecimal discount = coupon.getDiscountAmount() == null ? BigDecimal.ZERO : coupon.getDiscountAmount();
        if (discount.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        if (discount.compareTo(orderAmount) > 0) {
            return orderAmount;
        }
        return discount;
    }

    public void useCoupon(Long userId, Long userCouponId, Long orderId) {
        if (userCouponId == null) {
            return;
        }
        if (userCouponMapper.markUsed(userCouponId, userId, orderId) == 0) {
            throw new BusinessException("优惠券状态已变化，请刷新后重试");
        }
    }

    public void releaseByOrderId(Long orderId) {
        userCouponMapper.releaseByOrderId(orderId);
    }

    private void assertClaimable(Coupon coupon, LocalDateTime now) {
        if (coupon == null || coupon.getStatus() == null || coupon.getStatus() != 1) {
            throw new BusinessException("优惠券不存在");
        }
        if (coupon.getStartTime() != null && now.isBefore(coupon.getStartTime())) {
            throw new BusinessException("优惠券活动尚未开始");
        }
        if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
            throw new BusinessException("优惠券活动已结束");
        }
    }

    private void assertUsable(Coupon coupon, Long storeId, BigDecimal goodsAmount, LocalDateTime now) {
        assertClaimable(coupon, now);
        if (coupon.getStoreId() != null && !coupon.getStoreId().equals(storeId)) {
            throw new BusinessException("优惠券不适用于当前门店");
        }
        BigDecimal threshold = coupon.getThresholdAmount() == null ? BigDecimal.ZERO : coupon.getThresholdAmount();
        if (goodsAmount.compareTo(threshold) < 0) {
            throw new BusinessException("未达到优惠券使用门槛");
        }
    }
}
