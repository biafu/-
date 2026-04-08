package com.zhinengpt.campuscatering.cache.job;

import com.zhinengpt.campuscatering.merchant.dto.StoreSimpleResponse;
import com.zhinengpt.campuscatering.merchant.service.MerchantService;
import com.zhinengpt.campuscatering.product.dto.ProductViewResponse;
import com.zhinengpt.campuscatering.product.dto.SkuView;
import com.zhinengpt.campuscatering.product.service.ProductService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CachePrewarmJob {

    private static final Logger log = LoggerFactory.getLogger(CachePrewarmJob.class);

    private final MerchantService merchantService;
    private final ProductService productService;

    @Value("${app.cache.prewarm-store-limit:20}")
    private int prewarmStoreLimit;

    public CachePrewarmJob(MerchantService merchantService, ProductService productService) {
        this.merchantService = merchantService;
        this.productService = productService;
    }

    @Scheduled(cron = "${app.cache.prewarm-cron:0 */30 * * * *}")
    public void cachePrewarmJob() {
        List<StoreSimpleResponse> stores = merchantService.listStores();
        int warmedStoreCount = 0;
        int warmedSkuCount = 0;
        for (StoreSimpleResponse store : stores) {
            if (warmedStoreCount >= prewarmStoreLimit) {
                break;
            }
            if (store.getBusinessStatus() == null || store.getBusinessStatus() != 1) {
                continue;
            }
            try {
                merchantService.getStoreDetail(store.getId());
                warmedStoreCount++;

                List<ProductViewResponse> products = productService.listStudentProducts(store.getId());
                for (ProductViewResponse product : products) {
                    if (product.getSkus() == null) {
                        continue;
                    }
                    for (SkuView sku : product.getSkus()) {
                        productService.getSkuByIdCached(sku.getSkuId());
                        warmedSkuCount++;
                    }
                }
            } catch (Exception ex) {
                log.warn("Cache prewarm failed for storeId={}, cause={}", store.getId(), ex.getMessage());
            }
        }
        log.info("Cache prewarm finished. warmedStores={}, warmedSkus={}", warmedStoreCount, warmedSkuCount);
    }
}
