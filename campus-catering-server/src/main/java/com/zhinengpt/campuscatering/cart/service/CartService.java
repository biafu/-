package com.zhinengpt.campuscatering.cart.service;

import com.zhinengpt.campuscatering.cart.dto.CartAddRequest;
import com.zhinengpt.campuscatering.cart.dto.CartItemResponse;
import com.zhinengpt.campuscatering.cart.entity.ShoppingCart;
import com.zhinengpt.campuscatering.cart.mapper.ShoppingCartMapper;
import com.zhinengpt.campuscatering.common.exception.BusinessException;
import com.zhinengpt.campuscatering.product.entity.ProductSku;
import com.zhinengpt.campuscatering.product.entity.ProductSpu;
import com.zhinengpt.campuscatering.product.service.ProductService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    private final ShoppingCartMapper shoppingCartMapper;
    private final ProductService productService;

    public CartService(ShoppingCartMapper shoppingCartMapper, ProductService productService) {
        this.shoppingCartMapper = shoppingCartMapper;
        this.productService = productService;
    }

    public void add(Long userId, CartAddRequest request) {
        ProductSku sku = productService.getSkuByIdCached(request.getSkuId());
        ProductSpu spu = productService.getSpuById(sku.getSpuId());
        if (!spu.getStoreId().equals(request.getStoreId())) {
            throw new BusinessException("购物车商品与门店不匹配");
        }
        if (spu.getSaleStatus() == null || spu.getSaleStatus() != 1 || sku.getStatus() == null || sku.getStatus() != 1) {
            throw new BusinessException("商品已下架");
        }
        if (sku.getStock() < request.getQuantity()) {
            throw new BusinessException("库存不足");
        }
        ShoppingCart existed = shoppingCartMapper.selectByUserIdAndSkuId(userId, request.getSkuId());
        if (existed == null) {
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);
            shoppingCart.setStoreId(request.getStoreId());
            shoppingCart.setSkuId(request.getSkuId());
            shoppingCart.setQuantity(request.getQuantity());
            shoppingCartMapper.insert(shoppingCart);
            return;
        }
        existed.setQuantity(existed.getQuantity() + request.getQuantity());
        shoppingCartMapper.updateQuantity(existed);
    }

    public List<CartItemResponse> list(Long userId) {
        return shoppingCartMapper.selectByUserId(userId).stream()
                .map(cart -> {
                    ProductSku sku = productService.getSkuByIdCached(cart.getSkuId());
                    ProductSpu spu = productService.getSpuById(sku.getSpuId());
                    BigDecimal totalAmount = sku.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
                    return CartItemResponse.builder()
                            .id(cart.getId())
                            .storeId(cart.getStoreId())
                            .skuId(cart.getSkuId())
                            .productName(spu.getProductName())
                            .imageUrl(spu.getImageUrl())
                            .skuName(sku.getSkuName())
                            .price(sku.getPrice())
                            .quantity(cart.getQuantity())
                            .totalAmount(totalAmount)
                            .build();
                })
                .toList();
    }

    public void updateQuantity(Long userId, Long cartId, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new BusinessException("数量至少为1");
        }
        ShoppingCart cart = shoppingCartMapper.selectByIdAndUserId(cartId, userId);
        if (cart == null) {
            throw new BusinessException("购物车商品不存在");
        }
        ProductSku sku = productService.getSkuByIdCached(cart.getSkuId());
        ProductSpu spu = productService.getSpuById(sku.getSpuId());
        if (spu.getSaleStatus() == null || spu.getSaleStatus() != 1 || sku.getStatus() == null || sku.getStatus() != 1) {
            throw new BusinessException("商品已下架");
        }
        if (sku.getStock() < quantity) {
            throw new BusinessException("库存不足");
        }
        cart.setQuantity(quantity);
        int updated = shoppingCartMapper.updateQuantity(cart);
        if (updated == 0) {
            throw new BusinessException("购物车商品不存在");
        }
    }

    public void removeItem(Long userId, Long cartId) {
        int deleted = shoppingCartMapper.deleteByIdAndUserId(cartId, userId);
        if (deleted == 0) {
            throw new BusinessException("购物车商品不存在");
        }
    }

    public void clearStoreCart(Long userId, Long storeId) {
        shoppingCartMapper.deleteByUserIdAndStoreId(userId, storeId);
    }
}
