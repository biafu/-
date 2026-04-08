package com.zhinengpt.campuscatering.product.mapper;

import com.zhinengpt.campuscatering.product.entity.ProductCategory;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductCategoryMapper {

    @Insert("""
            insert into product_category(store_id, category_name, sort_no, status, created_at, updated_at)
            values(#{storeId}, #{categoryName}, #{sortNo}, #{status}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductCategory productCategory);

    @Select("""
            select id, store_id, category_name, sort_no, status, created_at, updated_at
            from product_category
            where store_id = #{storeId} and status = 1
            order by sort_no asc, id asc
            """)
    List<ProductCategory> selectByStoreId(Long storeId);

    @Select("""
            select id, store_id, category_name, sort_no, status, created_at, updated_at
            from product_category
            where id = #{id}
            limit 1
            """)
    ProductCategory selectById(Long id);
}
