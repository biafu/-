package com.zhinengpt.campuscatering.student.controller;

import com.zhinengpt.campuscatering.common.response.ApiResponse;
import com.zhinengpt.campuscatering.merchant.dto.StoreSimpleResponse;
import com.zhinengpt.campuscatering.merchant.service.MerchantService;
import com.zhinengpt.campuscatering.product.dto.ProductViewResponse;
import com.zhinengpt.campuscatering.product.service.ProductService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class StudentStoreController {

    private final MerchantService merchantService;
    private final ProductService productService;

    public StudentStoreController(MerchantService merchantService, ProductService productService) {
        this.merchantService = merchantService;
        this.productService = productService;
    }

    @GetMapping("/store/list")
    public ApiResponse<List<StoreSimpleResponse>> storeList() {
        return ApiResponse.success(merchantService.listStores());
    }

    @GetMapping("/store/{id}")
    public ApiResponse<StoreSimpleResponse> storeDetail(@PathVariable Long id) {
        return ApiResponse.success(merchantService.getStoreDetail(id));
    }

    @GetMapping("/product/list")
    public ApiResponse<List<ProductViewResponse>> productList(@RequestParam Long storeId) {
        return ApiResponse.success(productService.listStudentProducts(storeId));
    }
}
