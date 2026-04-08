package com.zhinengpt.campuscatering.auth.service;

import com.zhinengpt.campuscatering.auth.dto.CurrentUserResponse;
import com.zhinengpt.campuscatering.auth.dto.LoginRequest;
import com.zhinengpt.campuscatering.auth.dto.LoginResponse;
import com.zhinengpt.campuscatering.auth.entity.DeliveryUser;
import com.zhinengpt.campuscatering.auth.entity.MerchantUser;
import com.zhinengpt.campuscatering.auth.entity.StudentUser;
import com.zhinengpt.campuscatering.auth.entity.SysAdmin;
import com.zhinengpt.campuscatering.auth.mapper.DeliveryUserMapper;
import com.zhinengpt.campuscatering.auth.mapper.MerchantUserMapper;
import com.zhinengpt.campuscatering.auth.mapper.StudentUserMapper;
import com.zhinengpt.campuscatering.auth.mapper.SysAdminMapper;
import com.zhinengpt.campuscatering.common.enums.UserRole;
import com.zhinengpt.campuscatering.common.exception.BusinessException;
import com.zhinengpt.campuscatering.security.JwtTokenService;
import com.zhinengpt.campuscatering.security.LoginUser;
import com.zhinengpt.campuscatering.security.SecurityUtils;
import com.zhinengpt.campuscatering.security.TokenBlacklistService;
import io.jsonwebtoken.JwtException;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final StudentUserMapper studentUserMapper;
    private final MerchantUserMapper merchantUserMapper;
    private final SysAdminMapper sysAdminMapper;
    private final DeliveryUserMapper deliveryUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(StudentUserMapper studentUserMapper,
                       MerchantUserMapper merchantUserMapper,
                       SysAdminMapper sysAdminMapper,
                       DeliveryUserMapper deliveryUserMapper,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService jwtTokenService,
                       TokenBlacklistService tokenBlacklistService) {
        this.studentUserMapper = studentUserMapper;
        this.merchantUserMapper = merchantUserMapper;
        this.sysAdminMapper = sysAdminMapper;
        this.deliveryUserMapper = deliveryUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public LoginResponse studentLogin(LoginRequest request) {
        StudentUser user = studentUserMapper.selectByPhone(request.getUsername());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(4010, "学生账号不存在或已禁用");
        }
        checkPassword(request.getPassword(), user.getPassword());
        return buildResponse(LoginUser.builder()
                .userId(user.getId())
                .username(user.getNickname())
                .role(UserRole.STUDENT)
                .build());
    }

    public LoginResponse merchantLogin(LoginRequest request) {
        MerchantUser user = merchantUserMapper.selectByUsername(request.getUsername());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(4010, "商家账号不存在或已禁用");
        }
        checkPassword(request.getPassword(), user.getPassword());
        return buildResponse(LoginUser.builder()
                .userId(user.getId())
                .merchantId(user.getMerchantId())
                .username(user.getUsername())
                .role(UserRole.MERCHANT)
                .build());
    }

    public LoginResponse adminLogin(LoginRequest request) {
        SysAdmin user = sysAdminMapper.selectByUsername(request.getUsername());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(4010, "管理员账号不存在或已禁用");
        }
        checkPassword(request.getPassword(), user.getPassword());
        return buildResponse(LoginUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(UserRole.ADMIN)
                .build());
    }

    public LoginResponse deliveryLogin(LoginRequest request) {
        DeliveryUser user = deliveryUserMapper.selectByUsername(request.getUsername());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(4010, "配送员账号不存在或已禁用");
        }
        checkPassword(request.getPassword(), user.getPassword());
        return buildResponse(LoginUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(UserRole.DELIVERY)
                .build());
    }

    public CurrentUserResponse currentUser() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return CurrentUserResponse.builder()
                .userId(loginUser.getUserId())
                .merchantId(loginUser.getMerchantId())
                .username(loginUser.getUsername())
                .role(loginUser.getRole())
                .build();
    }

    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BusinessException(4010, "登录状态已失效");
        }
        String token = authorizationHeader.substring(7);
        try {
            Instant expireAt = jwtTokenService.parseExpiration(token);
            tokenBlacklistService.blacklist(token, expireAt);
        } catch (JwtException ex) {
            throw new BusinessException(4010, "登录状态已失效");
        }
    }

    private void checkPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BusinessException(4010, "用户名或密码错误");
        }
    }

    private LoginResponse buildResponse(LoginUser loginUser) {
        return LoginResponse.builder()
                .userId(loginUser.getUserId())
                .merchantId(loginUser.getMerchantId())
                .username(loginUser.getUsername())
                .role(loginUser.getRole())
                .token(jwtTokenService.generateToken(loginUser))
                .build();
    }
}
