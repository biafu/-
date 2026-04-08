package com.zhinengpt.campuscatering.student.mapper;

import com.zhinengpt.campuscatering.student.entity.UserAddress;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserAddressMapper {

    @Insert("""
            insert into user_address(user_id, campus_name, building_name, room_no, detail_address, contact_name, contact_phone, is_default, status, created_at, updated_at)
            values(#{userId}, #{campusName}, #{buildingName}, #{roomNo}, #{detailAddress}, #{contactName}, #{contactPhone}, #{isDefault}, #{status}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserAddress userAddress);

    @Select("""
            select id, user_id, campus_name, building_name, room_no, detail_address, contact_name, contact_phone, is_default, status, created_at, updated_at
            from user_address
            where user_id = #{userId} and status = 1
            order by is_default desc, id desc
            """)
    List<UserAddress> selectByUserId(Long userId);

    @Select("""
            select id, user_id, campus_name, building_name, room_no, detail_address, contact_name, contact_phone, is_default, status, created_at, updated_at
            from user_address
            where id = #{id} and user_id = #{userId} and status = 1
            limit 1
            """)
    UserAddress selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Update("""
            update user_address
            set campus_name = #{campusName},
                building_name = #{buildingName},
                room_no = #{roomNo},
                detail_address = #{detailAddress},
                contact_name = #{contactName},
                contact_phone = #{contactPhone},
                is_default = #{isDefault},
                updated_at = now()
            where id = #{id} and user_id = #{userId} and status = 1
            """)
    int updateByIdAndUserId(UserAddress userAddress);

    @Update("""
            update user_address
            set is_default = 0, updated_at = now()
            where user_id = #{userId} and status = 1
            """)
    int clearDefaultByUserId(Long userId);

    @Update("""
            update user_address
            set is_default = 1, updated_at = now()
            where id = #{id} and user_id = #{userId} and status = 1
            """)
    int setDefault(@Param("id") Long id, @Param("userId") Long userId);

    @Update("""
            update user_address
            set status = 0, updated_at = now()
            where id = #{id} and user_id = #{userId} and status = 1
            """)
    int softDelete(@Param("id") Long id, @Param("userId") Long userId);

    @Select("""
            select id, user_id, campus_name, building_name, room_no, detail_address, contact_name, contact_phone, is_default, status, created_at, updated_at
            from user_address
            where user_id = #{userId} and status = 1 and is_default = 1
            order by id desc
            limit 1
            """)
    UserAddress selectDefaultByUserId(Long userId);
}
