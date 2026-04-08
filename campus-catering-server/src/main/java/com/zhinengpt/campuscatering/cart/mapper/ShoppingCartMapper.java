package com.zhinengpt.campuscatering.cart.mapper;

import com.zhinengpt.campuscatering.cart.entity.ShoppingCart;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ShoppingCartMapper {

    @Select("""
            select id, user_id, store_id, sku_id, quantity, created_at, updated_at
            from shopping_cart
            where user_id = #{userId} and sku_id = #{skuId}
            limit 1
            """)
    ShoppingCart selectByUserIdAndSkuId(@Param("userId") Long userId, @Param("skuId") Long skuId);

    @Select("""
            select id, user_id, store_id, sku_id, quantity, created_at, updated_at
            from shopping_cart
            where id = #{id} and user_id = #{userId}
            limit 1
            """)
    ShoppingCart selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Insert("""
            insert into shopping_cart(user_id, store_id, sku_id, quantity, created_at, updated_at)
            values(#{userId}, #{storeId}, #{skuId}, #{quantity}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ShoppingCart shoppingCart);

    @Update("""
            update shopping_cart
            set quantity = #{quantity}, updated_at = now()
            where id = #{id} and user_id = #{userId}
            """)
    int updateQuantity(ShoppingCart shoppingCart);

    @Select("""
            select id, user_id, store_id, sku_id, quantity, created_at, updated_at
            from shopping_cart
            where user_id = #{userId}
            order by id desc
            """)
    List<ShoppingCart> selectByUserId(Long userId);

    @Delete("delete from shopping_cart where user_id = #{userId} and store_id = #{storeId}")
    int deleteByUserIdAndStoreId(@Param("userId") Long userId, @Param("storeId") Long storeId);

    @Delete("delete from shopping_cart where id = #{id} and user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
