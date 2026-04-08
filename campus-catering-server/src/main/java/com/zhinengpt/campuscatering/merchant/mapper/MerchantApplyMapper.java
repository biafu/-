package com.zhinengpt.campuscatering.merchant.mapper;

import com.zhinengpt.campuscatering.merchant.entity.MerchantApply;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MerchantApplyMapper {

    @Insert("""
            insert into merchant_apply(merchant_name, contact_name, contact_phone, campus_code, license_no, status, audit_remark, created_at, updated_at)
            values(#{merchantName}, #{contactName}, #{contactPhone}, #{campusCode}, #{licenseNo}, #{status}, #{auditRemark}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MerchantApply merchantApply);

    @Select("""
            select id, merchant_name, contact_name, contact_phone, campus_code, license_no, status, audit_remark, created_at, updated_at
            from merchant_apply
            order by id desc
            """)
    List<MerchantApply> selectAll();

    @Select("""
            select id, merchant_name, contact_name, contact_phone, campus_code, license_no, status, audit_remark, created_at, updated_at
            from merchant_apply
            where id = #{id}
            limit 1
            """)
    MerchantApply selectById(Long id);

    @Update("""
            update merchant_apply
            set status = #{status}, audit_remark = #{auditRemark}, updated_at = now()
            where id = #{id}
            """)
    int updateAudit(MerchantApply merchantApply);
}
