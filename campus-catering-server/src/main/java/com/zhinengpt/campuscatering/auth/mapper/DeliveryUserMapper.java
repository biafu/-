package com.zhinengpt.campuscatering.auth.mapper;

import com.zhinengpt.campuscatering.auth.entity.DeliveryUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DeliveryUserMapper {

    @Select("""
            select id, username, password, real_name, phone, status, created_at, updated_at
            from delivery_user
            where username = #{username}
            limit 1
            """)
    DeliveryUser selectByUsername(String username);

    @Select("""
            select id, username, password, real_name, phone, status, created_at, updated_at
            from delivery_user
            where id = #{id}
            limit 1
            """)
    DeliveryUser selectById(Long id);
}
