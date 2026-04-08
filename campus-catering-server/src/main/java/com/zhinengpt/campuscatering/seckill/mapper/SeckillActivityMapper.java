package com.zhinengpt.campuscatering.seckill.mapper;

import com.zhinengpt.campuscatering.seckill.entity.SeckillActivity;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillActivityMapper {

    @Select("""
            select id, store_id, sku_id, activity_name, seckill_price, stock, start_time, end_time, status, created_at, updated_at
            from seckill_activity
            where id = #{id}
            limit 1
            """)
    SeckillActivity selectById(Long id);

    @Select("""
            select id, store_id, sku_id, activity_name, seckill_price, stock, start_time, end_time, status, created_at, updated_at
            from seckill_activity
            order by created_at desc, id desc
            """)
    List<SeckillActivity> selectAllList();

    @Select("""
            select id, store_id, sku_id, activity_name, seckill_price, stock, start_time, end_time, status, created_at, updated_at
            from seckill_activity
            where status = 1
            order by start_time asc, id desc
            """)
    List<SeckillActivity> selectEnabledList();

    @Select("""
            select id, store_id, sku_id, activity_name, seckill_price, stock, start_time, end_time, status, created_at, updated_at
            from seckill_activity
            where store_id = #{storeId}
              and status = 1
              and start_time <= #{now}
              and end_time >= #{now}
            order by id desc
            """)
    List<SeckillActivity> selectActiveByStoreId(@Param("storeId") Long storeId, @Param("now") LocalDateTime now);

    @Insert("""
            insert into seckill_activity(store_id, sku_id, activity_name, seckill_price, stock, start_time, end_time, status, created_at, updated_at)
            values(#{storeId}, #{skuId}, #{activityName}, #{seckillPrice}, #{stock}, #{startTime}, #{endTime}, #{status}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SeckillActivity activity);

    @Update("""
            update seckill_activity
            set store_id = #{storeId},
                sku_id = #{skuId},
                activity_name = #{activityName},
                seckill_price = #{seckillPrice},
                stock = #{stock},
                start_time = #{startTime},
                end_time = #{endTime},
                status = #{status},
                updated_at = now()
            where id = #{id}
            """)
    int updateById(SeckillActivity activity);

    @Delete("delete from seckill_activity where id = #{id}")
    int deleteById(Long id);
}
