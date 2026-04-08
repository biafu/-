package com.zhinengpt.campuscatering.cart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.zhinengpt.campuscatering.cart.entity.ShoppingCart;
import com.zhinengpt.campuscatering.cart.mapper.ShoppingCartMapper;
import com.zhinengpt.campuscatering.common.exception.BusinessException;
import com.zhinengpt.campuscatering.product.entity.ProductSku;
import com.zhinengpt.campuscatering.product.entity.ProductSpu;
import com.zhinengpt.campuscatering.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ShoppingCartMapper shoppingCartMapper;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartService cartService;

    @Test
    void updateQuantity_updatesAnOwnedCartItemAndChangesQuantity() {
        ShoppingCart cart = new ShoppingCart();
        cart.setId(8L);
        cart.setUserId(1001L);
        cart.setSkuId(501L);
        cart.setQuantity(2);

        ProductSku sku = new ProductSku();
        sku.setId(501L);
        sku.setSpuId(601L);
        sku.setStock(10);
        sku.setStatus(1);

        ProductSpu spu = new ProductSpu();
        spu.setId(601L);
        spu.setStoreId(701L);
        spu.setSaleStatus(1);

        when(shoppingCartMapper.selectByIdAndUserId(8L, 1001L)).thenReturn(cart);
        when(productService.getSkuByIdCached(501L)).thenReturn(sku);
        when(productService.getSpuById(601L)).thenReturn(spu);
        when(shoppingCartMapper.updateQuantity(cart)).thenReturn(1);

        cartService.updateQuantity(1001L, 8L, 5);

        ArgumentCaptor<ShoppingCart> captor = ArgumentCaptor.forClass(ShoppingCart.class);
        verify(shoppingCartMapper).updateQuantity(captor.capture());
        ShoppingCart updated = captor.getValue();
        assertEquals(5, updated.getQuantity());
        assertEquals(1001L, updated.getUserId());
        verify(productService).getSkuByIdCached(501L);
        verify(productService).getSpuById(601L);
    }

    @Test
    void updateQuantity_rejectsQuantityLessThanOne() {
        assertThrows(BusinessException.class, () -> cartService.updateQuantity(1001L, 8L, 0));
        verify(shoppingCartMapper, never()).updateQuantity(any());
        verifyNoInteractions(productService);
    }

    @Test
    void updateQuantity_revalidatesSaleStatusBeforeUpdate() {
        ShoppingCart cart = new ShoppingCart();
        cart.setId(8L);
        cart.setUserId(1001L);
        cart.setSkuId(501L);
        cart.setQuantity(2);

        ProductSku sku = new ProductSku();
        sku.setId(501L);
        sku.setSpuId(601L);
        sku.setStock(10);
        sku.setStatus(1);

        ProductSpu spu = new ProductSpu();
        spu.setId(601L);
        spu.setStoreId(701L);
        spu.setSaleStatus(0);

        when(shoppingCartMapper.selectByIdAndUserId(8L, 1001L)).thenReturn(cart);
        when(productService.getSkuByIdCached(501L)).thenReturn(sku);
        when(productService.getSpuById(601L)).thenReturn(spu);

        assertThrows(BusinessException.class, () -> cartService.updateQuantity(1001L, 8L, 5));
        verify(shoppingCartMapper, never()).updateQuantity(any());
    }

    @Test
    void updateQuantity_revalidatesStockBeforeUpdate() {
        ShoppingCart cart = new ShoppingCart();
        cart.setId(8L);
        cart.setUserId(1001L);
        cart.setSkuId(501L);
        cart.setQuantity(2);

        ProductSku sku = new ProductSku();
        sku.setId(501L);
        sku.setSpuId(601L);
        sku.setStock(4);
        sku.setStatus(1);

        ProductSpu spu = new ProductSpu();
        spu.setId(601L);
        spu.setStoreId(701L);
        spu.setSaleStatus(1);

        when(shoppingCartMapper.selectByIdAndUserId(8L, 1001L)).thenReturn(cart);
        when(productService.getSkuByIdCached(501L)).thenReturn(sku);
        when(productService.getSpuById(601L)).thenReturn(spu);

        assertThrows(BusinessException.class, () -> cartService.updateQuantity(1001L, 8L, 5));
        verify(shoppingCartMapper, never()).updateQuantity(any());
    }

    @Test
    void updateQuantity_rejectsMissingCartItem() {
        when(shoppingCartMapper.selectByIdAndUserId(8L, 1001L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> cartService.updateQuantity(1001L, 8L, 5));
        verifyNoInteractions(productService);
        verify(shoppingCartMapper, never()).updateQuantity(any());
    }

    @Test
    void updateQuantity_detectsNoOpUpdateWhenRowDisappears() {
        ShoppingCart cart = new ShoppingCart();
        cart.setId(8L);
        cart.setUserId(1001L);
        cart.setSkuId(501L);
        cart.setQuantity(2);

        ProductSku sku = new ProductSku();
        sku.setId(501L);
        sku.setSpuId(601L);
        sku.setStock(10);
        sku.setStatus(1);

        ProductSpu spu = new ProductSpu();
        spu.setId(601L);
        spu.setStoreId(701L);
        spu.setSaleStatus(1);

        when(shoppingCartMapper.selectByIdAndUserId(8L, 1001L)).thenReturn(cart);
        when(productService.getSkuByIdCached(501L)).thenReturn(sku);
        when(productService.getSpuById(601L)).thenReturn(spu);
        when(shoppingCartMapper.updateQuantity(cart)).thenReturn(0);

        assertThrows(BusinessException.class, () -> cartService.updateQuantity(1001L, 8L, 5));
        verify(shoppingCartMapper).updateQuantity(cart);
    }

    @Test
    void removeItem_deletesAnOwnedCartItem() {
        when(shoppingCartMapper.deleteByIdAndUserId(9L, 1001L)).thenReturn(1);

        cartService.removeItem(1001L, 9L);

        verify(shoppingCartMapper).deleteByIdAndUserId(9L, 1001L);
    }

    @Test
    void removeItem_rejectsMissingCartItem() {
        when(shoppingCartMapper.deleteByIdAndUserId(9L, 1001L)).thenReturn(0);

        assertThrows(BusinessException.class, () -> cartService.removeItem(1001L, 9L));
    }

    @Test
    void clearStoreCart_delegatesToDeleteByUserIdAndStoreId() {
        cartService.clearStoreCart(1001L, 66L);

        verify(shoppingCartMapper).deleteByUserIdAndStoreId(1001L, 66L);
    }
}
