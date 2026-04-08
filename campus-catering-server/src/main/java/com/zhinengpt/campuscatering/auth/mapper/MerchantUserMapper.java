package com.zhinengpt.campuscatering.auth.mapper;

import com.zhinengpt.campuscatering.auth.entity.MerchantUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MerchantUserMapper {

    @Select("""
            select id, merchant_id, username, password, real_name, status, created_at, updated_at
            from merchant_user
            where username = #{username}
            limit 1
            """)
    MerchantUser selectByUsername(String username);

    @Insert("""
            insert into merchant_user(merchant_id, username, password, real_name, status, created_at, updated_at)
            values(#{merchantId}, #{username}, #{password}, #{realName}, #{status}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MerchantUser merchantUser);
}
