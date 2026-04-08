package com.zhinengpt.campuscatering.product.mapper;

import com.zhinengpt.campuscatering.product.entity.ProductSku;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductSkuMapper {

    @Insert("""
            insert into product_sku(spu_id, sku_name, price, stock, sold_num, version, status, created_at, updated_at)
            values(#{spuId}, #{skuName}, #{price}, #{stock}, #{soldNum}, #{version}, #{status}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductSku productSku);

    @Select("""
            select id, spu_id, sku_name, price, stock, sold_num, version, status, created_at, updated_at
            from product_sku
            where spu_id = #{spuId}
            order by id asc
            """)
    List<ProductSku> selectBySpuId(Long spuId);

    @Select("""
            select id, spu_id, sku_name, price, stock, sold_num, version, status, created_at, updated_at
            from product_sku
            where id = #{id}
            limit 1
            """)
    ProductSku selectById(Long id);

    @Update("""
            update product_sku
            set stock = stock - #{quantity},
                sold_num = sold_num + #{quantity},
                version = version + 1,
                updated_at = now()
            where id = #{id}
              and stock >= #{quantity}
              and version = #{version}
            """)
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity, @Param("version") Integer version);

    @Update("""
            update product_sku
            set stock = stock + #{quantity},
                sold_num = greatest(sold_num - #{quantity}, 0),
                updated_at = now()
            where id = #{id}
            """)
    int restoreStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Update("""
            update product_sku
            set sku_name = #{skuName},
                price = #{price},
                status = #{status},
                updated_at = now()
            where id = #{id}
            """)
    int updateBasic(ProductSku productSku);

    @Update("""
            update product_sku
            set stock = #{stock},
                version = version + 1,
                updated_at = now()
            where id = #{id}
            """)
    int updateStock(@Param("id") Long id, @Param("stock") Integer stock);

    @Delete("""
            delete from product_sku
            where spu_id = #{spuId}
            """)
    int deleteBySpuId(Long spuId);
}
