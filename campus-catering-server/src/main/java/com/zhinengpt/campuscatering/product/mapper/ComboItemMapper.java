package com.zhinengpt.campuscatering.product.mapper;

import com.zhinengpt.campuscatering.product.dto.ComboItemView;
import com.zhinengpt.campuscatering.product.entity.ComboItem;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ComboItemMapper {

    @Insert("""
            insert into combo_item(combo_id, sku_id, quantity, sort_no, created_at, updated_at)
            values(#{comboId}, #{skuId}, #{quantity}, #{sortNo}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ComboItem comboItem);

    @Select("""
            select id, combo_id, sku_id, quantity, sort_no, created_at, updated_at
            from combo_item
            where combo_id = #{comboId}
            order by sort_no asc, id asc
            """)
    List<ComboItem> selectByComboId(Long comboId);

    @Select("""
            select ci.sku_id as sku_id,
                   ps.product_name as spu_name,
                   sk.sku_name as sku_name,
                   ci.quantity as quantity,
                   ci.sort_no as sort_no
            from combo_item ci
            join product_sku sk on sk.id = ci.sku_id
            join product_spu ps on ps.id = sk.spu_id
            where ci.combo_id = #{comboId}
            order by ci.sort_no asc, ci.id asc
            """)
    List<ComboItemView> selectViewByComboId(Long comboId);

    @Delete("""
            delete from combo_item
            where combo_id = #{comboId}
            """)
    int deleteByComboId(Long comboId);
}
