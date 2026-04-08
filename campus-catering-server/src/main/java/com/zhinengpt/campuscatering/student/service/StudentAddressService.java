package com.zhinengpt.campuscatering.student.service;

import com.zhinengpt.campuscatering.common.exception.BusinessException;
import com.zhinengpt.campuscatering.student.dto.UserAddressResponse;
import com.zhinengpt.campuscatering.student.dto.UserAddressSaveRequest;
import com.zhinengpt.campuscatering.student.entity.UserAddress;
import com.zhinengpt.campuscatering.student.mapper.UserAddressMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class StudentAddressService {

    private final UserAddressMapper userAddressMapper;

    public StudentAddressService(UserAddressMapper userAddressMapper) {
        this.userAddressMapper = userAddressMapper;
    }

    public List<UserAddressResponse> list(Long userId) {
        return userAddressMapper.selectByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long save(Long userId, UserAddressSaveRequest request) {
        validateRequest(request);
        boolean shouldSetDefault = request.getIsDefault() != null && request.getIsDefault() == 1;

        if (request.getId() == null) {
            UserAddress userAddress = new UserAddress();
            userAddress.setUserId(userId);
            userAddress.setCampusName(request.getCampusName().trim());
            userAddress.setBuildingName(request.getBuildingName().trim());
            userAddress.setRoomNo(request.getRoomNo().trim());
            userAddress.setDetailAddress(trimToNull(request.getDetailAddress()));
            userAddress.setContactName(request.getContactName().trim());
            userAddress.setContactPhone(request.getContactPhone().trim());
            userAddress.setIsDefault(shouldSetDefault ? 1 : 0);
            userAddress.setStatus(1);
            if (shouldSetDefault || userAddressMapper.selectDefaultByUserId(userId) == null) {
                userAddressMapper.clearDefaultByUserId(userId);
                userAddress.setIsDefault(1);
            }
            userAddressMapper.insert(userAddress);
            return userAddress.getId();
        }

        UserAddress existed = userAddressMapper.selectByIdAndUserId(request.getId(), userId);
        if (existed == null) {
            throw new BusinessException("地址不存在");
        }
        boolean wasDefault = existed.getIsDefault() != null && existed.getIsDefault() == 1;
        existed.setCampusName(request.getCampusName().trim());
        existed.setBuildingName(request.getBuildingName().trim());
        existed.setRoomNo(request.getRoomNo().trim());
        existed.setDetailAddress(trimToNull(request.getDetailAddress()));
        existed.setContactName(request.getContactName().trim());
        existed.setContactPhone(request.getContactPhone().trim());
        existed.setIsDefault(shouldSetDefault ? 1 : 0);
        if (shouldSetDefault) {
            userAddressMapper.clearDefaultByUserId(userId);
            existed.setIsDefault(1);
        } else if (wasDefault) {
            UserAddress defaultAddress = userAddressMapper.selectDefaultByUserId(userId);
            if (defaultAddress == null || existed.getId().equals(defaultAddress.getId())) {
                existed.setIsDefault(1);
            }
        }
        userAddressMapper.updateByIdAndUserId(existed);
        return existed.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {
        UserAddress existed = userAddressMapper.selectByIdAndUserId(id, userId);
        if (existed == null) {
            throw new BusinessException("地址不存在");
        }
        if (userAddressMapper.softDelete(id, userId) == 0) {
            throw new BusinessException("删除地址失败");
        }
        if (existed.getIsDefault() != null && existed.getIsDefault() == 1) {
            List<UserAddress> addresses = userAddressMapper.selectByUserId(userId);
            if (!addresses.isEmpty()) {
                userAddressMapper.setDefault(addresses.get(0).getId(), userId);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long id) {
        if (userAddressMapper.selectByIdAndUserId(id, userId) == null) {
            throw new BusinessException("地址不存在");
        }
        userAddressMapper.clearDefaultByUserId(userId);
        userAddressMapper.setDefault(id, userId);
    }

    public UserAddress mustGetDefaultAddress(Long userId) {
        UserAddress userAddress = userAddressMapper.selectDefaultByUserId(userId);
        if (userAddress == null) {
            throw new BusinessException("请先维护默认收货地址");
        }
        return userAddress;
    }

    private void validateRequest(UserAddressSaveRequest request) {
        if (request.getIsDefault() == null || (request.getIsDefault() != 0 && request.getIsDefault() != 1)) {
            throw new BusinessException("默认地址标识非法");
        }
        if (!StringUtils.hasText(request.getCampusName())
                || !StringUtils.hasText(request.getBuildingName())
                || !StringUtils.hasText(request.getRoomNo())
                || !StringUtils.hasText(request.getContactName())
                || !StringUtils.hasText(request.getContactPhone())) {
            throw new BusinessException("地址关键信息不能为空");
        }
    }

    private UserAddressResponse toResponse(UserAddress address) {
        return UserAddressResponse.builder()
                .id(address.getId())
                .campusName(address.getCampusName())
                .buildingName(address.getBuildingName())
                .roomNo(address.getRoomNo())
                .detailAddress(address.getDetailAddress())
                .contactName(address.getContactName())
                .contactPhone(address.getContactPhone())
                .isDefault(address.getIsDefault())
                .fullAddress(buildFullAddress(address))
                .updatedAt(address.getUpdatedAt())
                .build();
    }

    private String buildFullAddress(UserAddress address) {
        StringBuilder builder = new StringBuilder();
        builder.append(address.getCampusName()).append(" ").append(address.getBuildingName()).append(" ").append(address.getRoomNo());
        if (StringUtils.hasText(address.getDetailAddress())) {
            builder.append(" ").append(address.getDetailAddress());
        }
        return builder.toString();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
