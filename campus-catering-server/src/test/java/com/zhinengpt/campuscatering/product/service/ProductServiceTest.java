package com.zhinengpt.campuscatering.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zhinengpt.campuscatering.cache.CacheClient;
import com.zhinengpt.campuscatering.common.exception.BusinessException;
import com.zhinengpt.campuscatering.merchant.entity.Store;
import com.zhinengpt.campuscatering.merchant.service.MerchantService;
import com.zhinengpt.campuscatering.product.dto.ComboItemSaveRequest;
import com.zhinengpt.campuscatering.product.dto.ComboUpdateRequest;
import com.zhinengpt.campuscatering.product.entity.Combo;
import com.zhinengpt.campuscatering.product.entity.ProductCategory;
import com.zhinengpt.campuscatering.product.entity.ProductSku;
import com.zhinengpt.campuscatering.product.entity.ProductSpu;
import com.zhinengpt.campuscatering.product.mapper.ComboItemMapper;
import com.zhinengpt.campuscatering.product.mapper.ComboMapper;
import com.zhinengpt.campuscatering.product.mapper.ProductCategoryMapper;
import com.zhinengpt.campuscatering.product.mapper.ProductSkuMapper;
import com.zhinengpt.campuscatering.product.mapper.ProductSpuMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductCategoryMapper productCategoryMapper;

    @Mock
    private ProductSpuMapper productSpuMapper;

    @Mock
    private ProductSkuMapper productSkuMapper;

    @Mock
    private ComboMapper comboMapper;

    @Mock
    private ComboItemMapper comboItemMapper;

    @Mock
    private MerchantService merchantService;

    @Mock
    private CacheClient cacheClient;

    @InjectMocks
    private ProductService productService;

    @Test
    void updateCombo_replacesDescriptionAndItemsForOwnedComboProduct() {
        Store store = new Store();
        store.setId(31L);

        ProductSpu comboProduct = new ProductSpu();
        comboProduct.setId(401L);
        comboProduct.setStoreId(31L);
        comboProduct.setProductType(2);
        comboProduct.setSaleStatus(1);

        ProductSpu singleProduct = new ProductSpu();
        singleProduct.setId(402L);
        singleProduct.setStoreId(31L);
        singleProduct.setProductType(1);
        singleProduct.setSaleStatus(1);

        ProductSku firstSku = new ProductSku();
        firstSku.setId(501L);
        firstSku.setSpuId(402L);
        firstSku.setStatus(1);

        ProductSku secondSku = new ProductSku();
        secondSku.setId(502L);
        secondSku.setSpuId(402L);
        secondSku.setStatus(1);

        Combo combo = new Combo();
        combo.setId(601L);
        combo.setSpuId(401L);
        combo.setComboDesc("old");
        combo.setStatus(1);

        ComboUpdateRequest request = new ComboUpdateRequest();
        request.setComboDesc("new combo");
        request.setComboItems(List.of(
                comboItem(501L, 1),
                comboItem(502L, 2)
        ));

        when(merchantService.findStoreByMerchantId(9L)).thenReturn(store);
        when(productSpuMapper.selectById(401L)).thenReturn(comboProduct);
        when(comboMapper.selectBySpuId(401L)).thenReturn(combo);
        when(productSkuMapper.selectById(501L)).thenReturn(firstSku);
        when(productSkuMapper.selectById(502L)).thenReturn(secondSku);
        when(productSpuMapper.selectById(402L)).thenReturn(singleProduct);

        productService.updateCombo(9L, 401L, request);

        ArgumentCaptor<Combo> comboCaptor = ArgumentCaptor.forClass(Combo.class);
        verify(comboMapper).updateBasic(comboCaptor.capture());
        assertEquals(601L, comboCaptor.getValue().getId());
        assertEquals("new combo", comboCaptor.getValue().getComboDesc());
        verify(comboItemMapper).deleteByComboId(601L);

        ArgumentCaptor<com.zhinengpt.campuscatering.product.entity.ComboItem> itemCaptor =
                ArgumentCaptor.forClass(com.zhinengpt.campuscatering.product.entity.ComboItem.class);
        verify(comboItemMapper, org.mockito.Mockito.times(2)).insert(itemCaptor.capture());
        List<com.zhinengpt.campuscatering.product.entity.ComboItem> savedItems = itemCaptor.getAllValues();
        assertEquals(501L, savedItems.get(0).getSkuId());
        assertEquals(1, savedItems.get(0).getSortNo());
        assertEquals(502L, savedItems.get(1).getSkuId());
        assertEquals(2, savedItems.get(1).getQuantity());
    }

    @Test
    void updateCombo_rejectsNonComboProducts() {
        Store store = new Store();
        store.setId(31L);

        ProductSpu singleProduct = new ProductSpu();
        singleProduct.setId(401L);
        singleProduct.setStoreId(31L);
        singleProduct.setProductType(1);

        ComboUpdateRequest request = new ComboUpdateRequest();
        request.setComboDesc("new combo");
        request.setComboItems(List.of(comboItem(501L, 1)));

        when(merchantService.findStoreByMerchantId(9L)).thenReturn(store);
        when(productSpuMapper.selectById(401L)).thenReturn(singleProduct);

        assertThrows(BusinessException.class, () -> productService.updateCombo(9L, 401L, request));
        verify(comboMapper, never()).updateBasic(any());
        verify(comboItemMapper, never()).deleteByComboId(any());
        verify(comboItemMapper, never()).insert(any());
    }

    private ComboItemSaveRequest comboItem(Long skuId, int quantity) {
        ComboItemSaveRequest item = new ComboItemSaveRequest();
        item.setSkuId(skuId);
        item.setQuantity(quantity);
        return item;
    }
}
