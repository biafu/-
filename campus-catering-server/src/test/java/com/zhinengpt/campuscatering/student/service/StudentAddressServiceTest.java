package com.zhinengpt.campuscatering.student.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhinengpt.campuscatering.student.dto.UserAddressResponse;
import com.zhinengpt.campuscatering.student.dto.UserAddressSaveRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class StudentAddressServiceTest {

    @Autowired
    private StudentAddressService studentAddressService;

    @Test
    void list_returnsAddressCollectionEvenWhenEmpty() {
        List<UserAddressResponse> addresses = studentAddressService.list(1L);

        assertNotNull(addresses);
    }

    @Test
    void save_createsANewAddressForStudent() {
        UserAddressSaveRequest request = new UserAddressSaveRequest();
        request.setCampusName("主校区");
        request.setBuildingName("东区宿舍");
        request.setRoomNo("8-502");
        request.setDetailAddress("靠近楼梯口");
        request.setContactName("张三");
        request.setContactPhone("13800000009");
        request.setIsDefault(1);

        Long addressId = studentAddressService.save(1L, request);
        List<UserAddressResponse> addresses = studentAddressService.list(1L);

        assertNotNull(addressId);
        assertTrue(addressId > 0);
        assertTrue(addresses.stream().anyMatch(item -> addressId.equals(item.getId())));
    }
}
