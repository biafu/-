package com.zhinengpt.campuscatering.merchant.mapper;

import com.zhinengpt.campuscatering.merchant.entity.StoreBusinessHours;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StoreBusinessHoursMapper {

    @Select("""
            select id, store_id, day_of_week, start_time, end_time, status, created_at, updated_at
            from store_business_hours
            where store_id = #{storeId}
            order by day_of_week asc, start_time asc, id asc
            """)
    List<StoreBusinessHours> selectByStoreId(Long storeId);

    @Select("""
            select id, store_id, day_of_week, start_time, end_time, status, created_at, updated_at
            from store_business_hours
            where store_id = #{storeId}
              and day_of_week = #{dayOfWeek}
              and status = 1
            order by start_time asc, id asc
            """)
    List<StoreBusinessHours> selectActiveByStoreAndDay(@Param("storeId") Long storeId, @Param("dayOfWeek") Integer dayOfWeek);

    @Delete("delete from store_business_hours where store_id = #{storeId}")
    int deleteByStoreId(Long storeId);

    @Insert("""
            insert into store_business_hours(store_id, day_of_week, start_time, end_time, status, created_at, updated_at)
            values(#{storeId}, #{dayOfWeek}, #{startTime}, #{endTime}, #{status}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StoreBusinessHours hours);
}
