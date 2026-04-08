package com.zhinengpt.campuscatering.auth.controller;

import com.zhinengpt.campuscatering.auth.dto.CurrentUserResponse;
import com.zhinengpt.campuscatering.auth.dto.LoginRequest;
import com.zhinengpt.campuscatering.auth.dto.LoginResponse;
import com.zhinengpt.campuscatering.auth.service.AuthService;
import com.zhinengpt.campuscatering.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/student/login")
    public ApiResponse<LoginResponse> studentLogin(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.studentLogin(request));
    }

    @PostMapping("/merchant/login")
    public ApiResponse<LoginResponse> merchantLogin(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.merchantLogin(request));
    }

    @PostMapping("/admin/login")
    public ApiResponse<LoginResponse> adminLogin(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.adminLogin(request));
    }

    @PostMapping("/delivery/login")
    public ApiResponse<LoginResponse> deliveryLogin(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.deliveryLogin(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logout(request.getHeader(HttpHeaders.AUTHORIZATION));
        return ApiResponse.success();
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me() {
        return ApiResponse.success(authService.currentUser());
    }
}
