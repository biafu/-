package com.zhinengpt.campuscatering.product.mapper;

import com.zhinengpt.campuscatering.product.entity.Combo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ComboMapper {

    @Insert("""
            insert into combo(spu_id, combo_desc, status, created_at, updated_at)
            values(#{spuId}, #{comboDesc}, #{status}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Combo combo);

    @Select("""
            select id, spu_id, combo_desc, status, created_at, updated_at
            from combo
            where spu_id = #{spuId}
            limit 1
            """)
    Combo selectBySpuId(Long spuId);

    @Update("""
            update combo
            set combo_desc = #{comboDesc},
                status = #{status},
                updated_at = now()
            where id = #{id}
            """)
    int updateBasic(Combo combo);

    @Delete("""
            delete from combo
            where spu_id = #{spuId}
            """)
    int deleteBySpuId(Long spuId);
}
