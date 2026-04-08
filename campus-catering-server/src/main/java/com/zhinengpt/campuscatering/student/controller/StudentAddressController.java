package com.zhinengpt.campuscatering.student.controller;

import com.zhinengpt.campuscatering.common.response.ApiResponse;
import com.zhinengpt.campuscatering.security.SecurityUtils;
import com.zhinengpt.campuscatering.student.dto.UserAddressResponse;
import com.zhinengpt.campuscatering.student.dto.UserAddressSaveRequest;
import com.zhinengpt.campuscatering.student.service.StudentAddressService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/address")
@PreAuthorize("hasRole('STUDENT')")
public class StudentAddressController {

    private final StudentAddressService studentAddressService;

    public StudentAddressController(StudentAddressService studentAddressService) {
        this.studentAddressService = studentAddressService;
    }

    @GetMapping("/list")
    public ApiResponse<List<UserAddressResponse>> list() {
        return ApiResponse.success(studentAddressService.list(SecurityUtils.getLoginUser().getUserId()));
    }

    @PostMapping("/save")
    public ApiResponse<Long> save(@Valid @RequestBody UserAddressSaveRequest request) {
        return ApiResponse.success(studentAddressService.save(SecurityUtils.getLoginUser().getUserId(), request));
    }

    @PostMapping("/default/{id}")
    public ApiResponse<Void> setDefault(@PathVariable Long id) {
        studentAddressService.setDefault(SecurityUtils.getLoginUser().getUserId(), id);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        studentAddressService.delete(SecurityUtils.getLoginUser().getUserId(), id);
        return ApiResponse.success();
    }
}
