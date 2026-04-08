package com.zhinengpt.campuscatering.merchant.mapper;

import com.zhinengpt.campuscatering.merchant.entity.Store;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StoreMapper {

    @Insert("""
            insert into store(merchant_id, store_name, address, delivery_type, delivery_scope_desc, delivery_radius_km, min_order_amount, delivery_fee, business_status, notice, deleted, created_at, updated_at)
            values(#{merchantId}, #{storeName}, #{address}, #{deliveryType}, #{deliveryScopeDesc}, #{deliveryRadiusKm}, #{minOrderAmount}, #{deliveryFee}, #{businessStatus}, #{notice}, 0, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Store store);

    @Select("""
            select id, merchant_id, store_name, address, delivery_type, delivery_scope_desc, delivery_radius_km, min_order_amount, delivery_fee, business_status, notice, deleted, created_at, updated_at
            from store
            where merchant_id = #{merchantId} and deleted = 0
            limit 1
            """)
    Store selectByMerchantId(Long merchantId);

    @Select("""
            select id, merchant_id, store_name, address, delivery_type, delivery_scope_desc, delivery_radius_km, min_order_amount, delivery_fee, business_status, notice, deleted, created_at, updated_at
            from store
            where id = #{id} and deleted = 0
            limit 1
            """)
    Store selectById(Long id);

    @Select("""
            select id, merchant_id, store_name, address, delivery_type, delivery_scope_desc, delivery_radius_km, min_order_amount, delivery_fee, business_status, notice, deleted, created_at, updated_at
            from store
            where deleted = 0
            order by id desc
            """)
    List<Store> selectAll();

    @Update("""
            update store
            set store_name = #{storeName},
                address = #{address},
                delivery_type = #{deliveryType},
                delivery_scope_desc = #{deliveryScopeDesc},
                delivery_radius_km = #{deliveryRadiusKm},
                min_order_amount = #{minOrderAmount},
                delivery_fee = #{deliveryFee},
                business_status = #{businessStatus},
                notice = #{notice},
                updated_at = now()
            where merchant_id = #{merchantId} and deleted = 0
            """)
    int updateByMerchantId(Store store);

    @Update("""
            update store
            set business_status = #{status}, updated_at = now()
            where id = #{storeId} and deleted = 0
            """)
    int updateBusinessStatus(@Param("storeId") Long storeId, @Param("status") Integer status);
}
