package com.zhinengpt.campuscatering.product.service;

import com.zhinengpt.campuscatering.cache.CacheClient;
import com.zhinengpt.campuscatering.common.exception.BusinessException;
import com.zhinengpt.campuscatering.merchant.entity.Store;
import com.zhinengpt.campuscatering.merchant.service.MerchantService;
import com.zhinengpt.campuscatering.product.dto.CategorySaveRequest;
import com.zhinengpt.campuscatering.product.dto.CategoryViewResponse;
import com.zhinengpt.campuscatering.product.dto.ComboItemSaveRequest;
import com.zhinengpt.campuscatering.product.dto.ComboItemView;
import com.zhinengpt.campuscatering.product.dto.ComboUpdateRequest;
import com.zhinengpt.campuscatering.product.dto.ProductBasicUpdateRequest;
import com.zhinengpt.campuscatering.product.dto.ProductEditorUpdateRequest;
import com.zhinengpt.campuscatering.product.dto.ProductSaveRequest;
import com.zhinengpt.campuscatering.product.dto.ProductSkuUpdateRequest;
import com.zhinengpt.campuscatering.product.dto.SkuStockUpdateRequest;
import com.zhinengpt.campuscatering.product.dto.ProductViewResponse;
import com.zhinengpt.campuscatering.product.dto.SkuSaveRequest;
import com.zhinengpt.campuscatering.product.dto.SkuView;
import com.zhinengpt.campuscatering.product.entity.Combo;
import com.zhinengpt.campuscatering.product.entity.ComboItem;
import com.zhinengpt.campuscatering.product.entity.ProductCategory;
import com.zhinengpt.campuscatering.product.entity.ProductSku;
import com.zhinengpt.campuscatering.product.entity.ProductSpu;
import com.zhinengpt.campuscatering.product.mapper.ComboItemMapper;
import com.zhinengpt.campuscatering.product.mapper.ComboMapper;
import com.zhinengpt.campuscatering.product.mapper.ProductCategoryMapper;
import com.zhinengpt.campuscatering.product.mapper.ProductSkuMapper;
import com.zhinengpt.campuscatering.product.mapper.ProductSpuMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProductService {

    private static final String SKU_CACHE_PREFIX = "product:detail:";
    private static final String SKU_LOCK_PREFIX = "lock:product:";

    private final ProductCategoryMapper productCategoryMapper;
    private final ProductSpuMapper productSpuMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ComboMapper comboMapper;
    private final ComboItemMapper comboItemMapper;
    private final MerchantService merchantService;
    private final CacheClient cacheClient;

    public ProductService(ProductCategoryMapper productCategoryMapper,
                          ProductSpuMapper productSpuMapper,
                          ProductSkuMapper productSkuMapper,
                          ComboMapper comboMapper,
                          ComboItemMapper comboItemMapper,
                          MerchantService merchantService,
                          CacheClient cacheClient) {
        this.productCategoryMapper = productCategoryMapper;
        this.productSpuMapper = productSpuMapper;
        this.productSkuMapper = productSkuMapper;
        this.comboMapper = comboMapper;
        this.comboItemMapper = comboItemMapper;
        this.merchantService = merchantService;
        this.cacheClient = cacheClient;
    }

    public Long saveCategory(Long merchantId, CategorySaveRequest request) {
        Store store = merchantService.findStoreByMerchantId(merchantId);
        ProductCategory category = new ProductCategory();
        category.setStoreId(store.getId());
        category.setCategoryName(request.getCategoryName());
        category.setSortNo(request.getSortNo());
        category.setStatus(1);
        productCategoryMapper.insert(category);
        return category.getId();
    }

    public List<CategoryViewResponse> listMerchantCategories(Long merchantId) {
        Store store = merchantService.findStoreByMerchantId(merchantId);
        return productCategoryMapper.selectByStoreId(store.getId()).stream()
                .map(category -> CategoryViewResponse.builder()
                        .id(category.getId())
                        .categoryName(category.getCategoryName())
                        .sortNo(category.getSortNo())
                        .build())
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long saveProduct(Long merchantId, ProductSaveRequest request) {
        Store store = merchantService.findStoreByMerchantId(merchantId);
        ProductCategory category = productCategoryMapper.selectById(request.getCategoryId());
        if (category == null || !store.getId().equals(category.getStoreId())) {
            throw new BusinessException("Category does not exist");
        }
        validateProductType(request.getProductType());
        if (request.getProductType() == 2) {
            validateComboItems(store.getId(), request.getComboItems());
        } else if (request.getComboItems() != null && !request.getComboItems().isEmpty()) {
            throw new BusinessException("Single product does not support combo items");
        }

        ProductSpu spu = new ProductSpu();
        spu.setStoreId(store.getId());
        spu.setCategoryId(request.getCategoryId());
        spu.setProductName(request.getProductName());
        spu.setProductType(request.getProductType());
        spu.setImageUrl(request.getImageUrl());
        spu.setDescription(request.getDescription());
        spu.setSaleStatus(1);
        spu.setSortNo(request.getSortNo());
        productSpuMapper.insert(spu);

        for (SkuSaveRequest skuRequest : request.getSkus()) {
            ProductSku sku = new ProductSku();
            sku.setSpuId(spu.getId());
            sku.setSkuName(skuRequest.getSkuName());
            sku.setPrice(skuRequest.getPrice());
            sku.setStock(skuRequest.getStock());
            sku.setSoldNum(0);
            sku.setVersion(0);
            sku.setStatus(1);
            productSkuMapper.insert(sku);
        }
        if (request.getProductType() == 2) {
            saveCombo(spu.getId(), request);
        }
        return spu.getId();
    }

    public List<ProductViewResponse> listStudentProducts(Long storeId) {
        List<ProductSpu> spus = productSpuMapper.selectByStoreId(storeId);
        List<ProductViewResponse> result = new ArrayList<>();
        for (ProductSpu spu : spus) {
            if (spu.getSaleStatus() == null || spu.getSaleStatus() != 1) {
                continue;
            }
            result.add(toView(spu, false));
        }
        return result;
    }

    public List<ProductViewResponse> listMerchantProducts(Long merchantId) {
        Store store = merchantService.findStoreByMerchantId(merchantId);
        return productSpuMapper.selectByStoreId(store.getId()).stream()
                .map(spu -> toView(spu, true))
                .toList();
    }

    public void onShelf(Long merchantId, Long productId) {
        ProductSpu spu = assertSpuOwnedByMerchant(merchantId, productId);
        productSpuMapper.updateSaleStatus(spu.getId(), 1);
    }

    public void offShelf(Long merchantId, Long productId) {
        ProductSpu spu = assertSpuOwnedByMerchant(merchantId, productId);
        productSpuMapper.updateSaleStatus(spu.getId(), 0);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProductBasic(Long merchantId, Long productId, ProductBasicUpdateRequest request) {
        ProductSpu spu = assertSpuOwnedByMerchant(merchantId, productId);
        ProductCategory category = productCategoryMapper.selectById(request.getCategoryId());
        if (category == null || !spu.getStoreId().equals(category.getStoreId())) {
            throw new BusinessException("Category does not exist");
        }
        spu.setCategoryId(request.getCategoryId());
        spu.setProductName(request.getProductName().trim());
        spu.setImageUrl(trimToNull(request.getImageUrl()));
        spu.setDescription(trimToNull(request.getDescription()));
        spu.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        productSpuMapper.updateBasic(spu);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProductFromEditor(Long merchantId, Long productId, ProductEditorUpdateRequest request) {
        ProductSpu spu = assertSpuOwnedByMerchant(merchantId, productId);
        ProductCategory category = productCategoryMapper.selectById(request.getCategoryId());
        if (category == null || !spu.getStoreId().equals(category.getStoreId())) {
            throw new BusinessException("Category does not exist");
        }

        ProductSku sku = assertSkuOwnedByMerchant(merchantId, request.getSkuId());
        if (!spu.getId().equals(sku.getSpuId())) {
            throw new BusinessException("閸熷棗鎼ф稉搴ゎ潐閺嶉棿绗夐崠褰掑帳");
        }
        if (request.getSkuStatus() != 0 && request.getSkuStatus() != 1) {
            throw new BusinessException("Invalid sku status");
        }

        spu.setCategoryId(request.getCategoryId());
        spu.setProductName(request.getProductName().trim());
        spu.setImageUrl(trimToNull(request.getImageUrl()));
        spu.setDescription(trimToNull(request.getDescription()));
        spu.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        productSpuMapper.updateBasic(spu);

        sku.setSkuName(request.getSkuName().trim());
        sku.setPrice(request.getPrice());
        sku.setStatus(request.getSkuStatus());
        productSkuMapper.updateBasic(sku);
        productSkuMapper.updateStock(sku.getId(), request.getStock());
        evictSkuCache(sku.getId());

        if (spu.getProductType() != null && spu.getProductType() == 2 && (request.getComboItems() != null || request.getComboDesc() != null)) {
            updateComboInternal(spu, request.getComboDesc(), request.getComboItems());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCombo(Long merchantId, Long productId, ComboUpdateRequest request) {
        ProductSpu spu = assertSpuOwnedByMerchant(merchantId, productId);
        validateProductType(spu.getProductType());
        if (spu.getProductType() != 2) {
            throw new BusinessException("瑜版挸澧犻崯鍡楁惂娑撳秵妲告總妤咁樀");
        }
        updateComboInternal(spu, request.getComboDesc(), request.getComboItems());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSku(Long merchantId, Long skuId, ProductSkuUpdateRequest request) {
        ProductSku sku = assertSkuOwnedByMerchant(merchantId, skuId);
        if (request.getStatus() == null || (request.getStatus() != 0 && request.getStatus() != 1)) {
            throw new BusinessException("Invalid sku status");
        }
        sku.setSkuName(request.getSkuName().trim());
        sku.setPrice(request.getPrice());
        sku.setStatus(request.getStatus());
        productSkuMapper.updateBasic(sku);
        evictSkuCache(skuId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSkuStock(Long merchantId, Long skuId, SkuStockUpdateRequest request) {
        assertSkuOwnedByMerchant(merchantId, skuId);
        productSkuMapper.updateStock(skuId, request.getStock());
        evictSkuCache(skuId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long merchantId, Long productId) {
        ProductSpu spu = assertSpuOwnedByMerchant(merchantId, productId);
        Combo combo = comboMapper.selectBySpuId(spu.getId());
        if (combo != null) {
            comboItemMapper.deleteByComboId(combo.getId());
            comboMapper.deleteBySpuId(spu.getId());
        }
        for (ProductSku sku : productSkuMapper.selectBySpuId(spu.getId())) {
            evictSkuCache(sku.getId());
        }
        productSkuMapper.deleteBySpuId(spu.getId());
        productSpuMapper.deleteById(spu.getId());
    }

    public ProductSku getSkuByIdCached(Long skuId) {
        ProductSku sku = cacheClient.queryWithMutex(
                SKU_CACHE_PREFIX + skuId,
                SKU_LOCK_PREFIX + skuId,
                ProductSku.class,
                Duration.ofMinutes(30),
                Duration.ofMinutes(3),
                ignored -> productSkuMapper.selectById(skuId)
        );
        if (sku == null) {
            throw new BusinessException("Sku does not exist");
        }
        return sku;
    }

    public ProductSku getSkuByIdFresh(Long skuId) {
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException("Sku does not exist");
        }
        return sku;
    }

    public ProductSpu getSpuById(Long spuId) {
        ProductSpu spu = productSpuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException("Product does not exist");
        }
        return spu;
    }

    public List<ComboItem> listComboItemsBySpuId(Long spuId) {
        Combo combo = comboMapper.selectBySpuId(spuId);
        if (combo == null || combo.getStatus() == null || combo.getStatus() != 1) {
            return List.of();
        }
        return comboItemMapper.selectByComboId(combo.getId());
    }

    public void evictSkuCache(Long skuId) {
        cacheClient.delete(SKU_CACHE_PREFIX + skuId);
    }

    private ProductSpu assertSpuOwnedByMerchant(Long merchantId, Long productId) {
        Store store = merchantService.findStoreByMerchantId(merchantId);
        ProductSpu spu = productSpuMapper.selectById(productId);
        if (spu == null || !store.getId().equals(spu.getStoreId())) {
            throw new BusinessException("鏃犳潈闄愭搷浣滆鑿滃搧");
        }
        return spu;
    }

    private ProductSku assertSkuOwnedByMerchant(Long merchantId, Long skuId) {
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException("Sku does not exist");
        }
        assertSpuOwnedByMerchant(merchantId, sku.getSpuId());
        return sku;
    }

    private ProductViewResponse toView(ProductSpu spu, boolean includeInactiveSku) {
        List<SkuView> skuViews = productSkuMapper.selectBySpuId(spu.getId()).stream()
                .filter(sku -> includeInactiveSku || (sku.getStatus() != null && sku.getStatus() == 1))
                .map(sku -> SkuView.builder()
                        .skuId(sku.getId())
                        .skuName(sku.getSkuName())
                        .price(sku.getPrice())
                        .stock(sku.getStock())
                        .status(sku.getStatus())
                        .build())
                .toList();
        String comboDesc = null;
        List<ComboItemView> comboItems = List.of();
        if (spu.getProductType() != null && spu.getProductType() == 2) {
            Combo combo = comboMapper.selectBySpuId(spu.getId());
            if (combo != null && combo.getStatus() != null && combo.getStatus() == 1) {
                comboDesc = combo.getComboDesc();
                comboItems = comboItemMapper.selectViewByComboId(combo.getId());
            }
        }
        return ProductViewResponse.builder()
                .spuId(spu.getId())
                .categoryId(spu.getCategoryId())
                .productName(spu.getProductName())
                .productType(spu.getProductType())
                .imageUrl(spu.getImageUrl())
                .description(spu.getDescription())
                .comboDesc(comboDesc)
                .saleStatus(spu.getSaleStatus())
                .sortNo(spu.getSortNo())
                .skus(skuViews)
                .comboItems(comboItems)
                .build();
    }

    private void validateProductType(Integer productType) {
        if (productType == null || (productType != 1 && productType != 2)) {
            throw new BusinessException("鍟嗗搧绫诲瀷浠呮敮鎸佸崟鍝佹垨濂楅");
        }
    }

    private void validateComboItems(Long storeId, List<ComboItemSaveRequest> comboItems) {
        if (comboItems == null || comboItems.isEmpty()) {
            throw new BusinessException("Combo must contain at least one item");
        }
        Set<Long> skuIds = new HashSet<>();
        for (ComboItemSaveRequest comboItem : comboItems) {
            if (!skuIds.add(comboItem.getSkuId())) {
                throw new BusinessException("濂楅鏄庣粏涓瓨鍦ㄩ噸澶嶇殑鍟嗗搧瑙勬牸");
            }
            ProductSku sku = productSkuMapper.selectById(comboItem.getSkuId());
            if (sku == null) {
                throw new BusinessException("Combo item sku does not exist");
            }
            ProductSpu spu = productSpuMapper.selectById(sku.getSpuId());
            if (spu == null || !storeId.equals(spu.getStoreId())) {
                throw new BusinessException("Combo item does not belong to current store");
            }
            if (spu.getProductType() != null && spu.getProductType() == 2) {
                throw new BusinessException("濂楅鏄庣粏鏆備笉鏀寔寮曠敤濂楅鍟嗗搧");
            }
            if (spu.getSaleStatus() == null || spu.getSaleStatus() != 1 || sku.getStatus() == null || sku.getStatus() != 1) {
                throw new BusinessException("Combo contains unavailable items");
            }
        }
    }

    private void saveCombo(Long spuId, ProductSaveRequest request) {
        Combo combo = new Combo();
        combo.setSpuId(spuId);
        combo.setComboDesc(request.getComboDesc());
        combo.setStatus(1);
        comboMapper.insert(combo);

        int defaultSortNo = 1;
        for (ComboItemSaveRequest comboItemRequest : request.getComboItems()) {
            ComboItem comboItem = new ComboItem();
            comboItem.setComboId(combo.getId());
            comboItem.setSkuId(comboItemRequest.getSkuId());
            comboItem.setQuantity(comboItemRequest.getQuantity());
            comboItem.setSortNo(comboItemRequest.getSortNo() == null ? defaultSortNo : comboItemRequest.getSortNo());
            comboItemMapper.insert(comboItem);
            defaultSortNo++;
        }
    }

    private void updateComboInternal(ProductSpu spu, String comboDesc, List<ComboItemSaveRequest> comboItems) {
        Combo combo = comboMapper.selectBySpuId(spu.getId());
        if (comboItems != null) {
            validateComboItems(spu.getStoreId(), comboItems);
        }

        if (combo == null) {
            combo = new Combo();
            combo.setSpuId(spu.getId());
            combo.setComboDesc(trimToNull(comboDesc));
            combo.setStatus(1);
            comboMapper.insert(combo);
        } else {
            combo.setComboDesc(comboDesc == null ? combo.getComboDesc() : trimToNull(comboDesc));
            combo.setStatus(1);
            comboMapper.updateBasic(combo);
        }

        if (comboItems == null) {
            return;
        }

        comboItemMapper.deleteByComboId(combo.getId());
        int defaultSortNo = 1;
        for (ComboItemSaveRequest comboItemRequest : comboItems) {
            ComboItem comboItem = new ComboItem();
            comboItem.setComboId(combo.getId());
            comboItem.setSkuId(comboItemRequest.getSkuId());
            comboItem.setQuantity(comboItemRequest.getQuantity());
            comboItem.setSortNo(comboItemRequest.getSortNo() == null ? defaultSortNo : comboItemRequest.getSortNo());
            comboItemMapper.insert(comboItem);
            defaultSortNo++;
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}

