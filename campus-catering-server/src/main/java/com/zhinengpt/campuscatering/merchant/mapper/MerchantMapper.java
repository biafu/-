package com.zhinengpt.campuscatering.merchant.mapper;

import com.zhinengpt.campuscatering.merchant.entity.Merchant;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MerchantMapper {

    @Insert("""
            insert into merchant(merchant_name, contact_name, contact_phone, status, settle_type, campus_code, created_at, updated_at)
            values(#{merchantName}, #{contactName}, #{contactPhone}, #{status}, #{settleType}, #{campusCode}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Merchant merchant);

    @Select("""
            select id, merchant_name, contact_name, contact_phone, status, settle_type, campus_code, created_at, updated_at
            from merchant
            where id = #{id}
            limit 1
            """)
    Merchant selectById(Long id);

    @Select("select count(1) from merchant where status = 1")
    Long countActiveMerchants();
}
