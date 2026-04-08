package com.zhinengpt.campuscatering.product.mapper;

import com.zhinengpt.campuscatering.product.entity.ProductSpu;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductSpuMapper {

    @Insert("""
            insert into product_spu(store_id, category_id, product_name, product_type, image_url, description, sale_status, sort_no, created_at, updated_at)
            values(#{storeId}, #{categoryId}, #{productName}, #{productType}, #{imageUrl}, #{description}, #{saleStatus}, #{sortNo}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductSpu productSpu);

    @Select("""
            select id, store_id, category_id, product_name, product_type, image_url, description, sale_status, sort_no, created_at, updated_at
            from product_spu
            where store_id = #{storeId}
            order by sort_no asc, id desc
            """)
    List<ProductSpu> selectByStoreId(Long storeId);

    @Select("""
            select id, store_id, category_id, product_name, product_type, image_url, description, sale_status, sort_no, created_at, updated_at
            from product_spu
            where id = #{id}
            limit 1
            """)
    ProductSpu selectById(Long id);

    @Update("""
            update product_spu
            set sale_status = #{saleStatus}, updated_at = now()
            where id = #{id}
            """)
    int updateSaleStatus(@Param("id") Long id, @Param("saleStatus") Integer saleStatus);

    @Update("""
            update product_spu
            set category_id = #{categoryId},
                product_name = #{productName},
                image_url = #{imageUrl},
                description = #{description},
                sort_no = #{sortNo},
                updated_at = now()
            where id = #{id}
            """)
    int updateBasic(ProductSpu productSpu);

    @Delete("""
            delete from product_spu
            where id = #{id}
            """)
    int deleteById(Long id);
}
