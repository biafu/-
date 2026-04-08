package com.zhinengpt.campuscatering.cart.controller;

import com.zhinengpt.campuscatering.cart.dto.CartAddRequest;
import com.zhinengpt.campuscatering.cart.dto.CartItemResponse;
import com.zhinengpt.campuscatering.cart.dto.CartQuantityUpdateRequest;
import com.zhinengpt.campuscatering.cart.service.CartService;
import com.zhinengpt.campuscatering.common.response.ApiResponse;
import com.zhinengpt.campuscatering.security.SecurityUtils;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/cart")
@PreAuthorize("hasRole('STUDENT')")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ApiResponse<Void> add(@Valid @RequestBody CartAddRequest request) {
        cartService.add(SecurityUtils.getLoginUser().getUserId(), request);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<List<CartItemResponse>> list() {
        return ApiResponse.success(cartService.list(SecurityUtils.getLoginUser().getUserId()));
    }

    @PatchMapping("/{id}/quantity")
    public ApiResponse<Void> updateQuantity(@PathVariable Long id, @Valid @RequestBody CartQuantityUpdateRequest request) {
        cartService.updateQuantity(SecurityUtils.getLoginUser().getUserId(), id, request.getQuantity());
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> removeItem(@PathVariable Long id) {
        cartService.removeItem(SecurityUtils.getLoginUser().getUserId(), id);
        return ApiResponse.success();
    }

    @DeleteMapping("/store/{storeId}")
    public ApiResponse<Void> clearStoreCart(@PathVariable Long storeId) {
        cartService.clearStoreCart(SecurityUtils.getLoginUser().getUserId(), storeId);
        return ApiResponse.success();
    }
}
