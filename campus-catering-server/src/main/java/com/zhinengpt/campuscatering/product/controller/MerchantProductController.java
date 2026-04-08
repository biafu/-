package com.zhinengpt.campuscatering.product.controller;

import com.zhinengpt.campuscatering.common.response.ApiResponse;
import com.zhinengpt.campuscatering.common.exception.BusinessException;
import com.zhinengpt.campuscatering.product.dto.CategorySaveRequest;
import com.zhinengpt.campuscatering.product.dto.CategoryViewResponse;
import com.zhinengpt.campuscatering.product.dto.ComboUpdateRequest;
import com.zhinengpt.campuscatering.product.dto.ProductBasicUpdateRequest;
import com.zhinengpt.campuscatering.product.dto.ProductEditorUpdateRequest;
import com.zhinengpt.campuscatering.product.dto.ProductSaveRequest;
import com.zhinengpt.campuscatering.product.dto.ProductSkuUpdateRequest;
import com.zhinengpt.campuscatering.product.dto.ProductViewResponse;
import com.zhinengpt.campuscatering.product.dto.SkuStockUpdateRequest;
import com.zhinengpt.campuscatering.product.service.ProductService;
import com.zhinengpt.campuscatering.security.SecurityUtils;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/merchant")
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantProductController {

    private final ProductService productService;
    private final String uploadDir;

    public MerchantProductController(ProductService productService,
                                     @Value("${app.storage.upload-dir:uploads}") String uploadDir) {
        this.productService = productService;
        this.uploadDir = uploadDir;
    }

    @PostMapping("/category/save")
    public ApiResponse<Long> saveCategory(@Valid @RequestBody CategorySaveRequest request) {
        return ApiResponse.success(productService.saveCategory(SecurityUtils.getLoginUser().getMerchantId(), request));
    }

    @PostMapping("/product/save")
    public ApiResponse<Long> saveProduct(@Valid @RequestBody ProductSaveRequest request) {
        return ApiResponse.success(productService.saveProduct(SecurityUtils.getLoginUser().getMerchantId(), request));
    }

    @GetMapping("/category/list")
    public ApiResponse<List<CategoryViewResponse>> listCategories() {
        return ApiResponse.success(productService.listMerchantCategories(SecurityUtils.getLoginUser().getMerchantId()));
    }

    @GetMapping("/product/list")
    public ApiResponse<List<ProductViewResponse>> list() {
        return ApiResponse.success(productService.listMerchantProducts(SecurityUtils.getLoginUser().getMerchantId()));
    }

    @PutMapping("/product/on-shelf/{id}")
    public ApiResponse<Void> onShelf(@PathVariable Long id) {
        productService.onShelf(SecurityUtils.getLoginUser().getMerchantId(), id);
        return ApiResponse.success();
    }

    @PutMapping("/product/off-shelf/{id}")
    public ApiResponse<Void> offShelf(@PathVariable Long id) {
        productService.offShelf(SecurityUtils.getLoginUser().getMerchantId(), id);
        return ApiResponse.success();
    }

    @PutMapping("/product/basic/{id}")
    public ApiResponse<Void> updateBasic(@PathVariable Long id, @Valid @RequestBody ProductBasicUpdateRequest request) {
        productService.updateProductBasic(SecurityUtils.getLoginUser().getMerchantId(), id, request);
        return ApiResponse.success();
    }

    @PutMapping("/product/{id}")
    public ApiResponse<Void> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductEditorUpdateRequest request) {
        productService.updateProductFromEditor(SecurityUtils.getLoginUser().getMerchantId(), id, request);
        return ApiResponse.success();
    }

    @PutMapping("/product/combo/{id}")
    public ApiResponse<Void> updateCombo(@PathVariable Long id, @Valid @RequestBody ComboUpdateRequest request) {
        productService.updateCombo(SecurityUtils.getLoginUser().getMerchantId(), id, request);
        return ApiResponse.success();
    }

    @PutMapping("/product/sku/{skuId}")
    public ApiResponse<Void> updateSku(@PathVariable Long skuId, @Valid @RequestBody ProductSkuUpdateRequest request) {
        productService.updateSku(SecurityUtils.getLoginUser().getMerchantId(), skuId, request);
        return ApiResponse.success();
    }

    @PutMapping("/product/stock/{skuId}")
    public ApiResponse<Void> updateStock(@PathVariable Long skuId, @Valid @RequestBody SkuStockUpdateRequest request) {
        productService.updateSkuStock(SecurityUtils.getLoginUser().getMerchantId(), skuId, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/product/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(SecurityUtils.getLoginUser().getMerchantId(), id);
        return ApiResponse.success();
    }

    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        String extension = ".jpg";
        if (originalName != null) {
            int idx = originalName.lastIndexOf(".");
            if (idx > -1 && idx < originalName.length() - 1) {
                extension = originalName.substring(idx).toLowerCase();
            }
        }
        Path targetDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(targetDir);
        String filename = UUID.randomUUID().toString().replace("-", "") + extension;
        Path targetFile = targetDir.resolve(filename);
        file.transferTo(targetFile.toFile());
        return ApiResponse.success("/uploads/" + filename);
    }
}
