package com.zhinengpt.campuscatering.auth.mapper;

import com.zhinengpt.campuscatering.auth.entity.StudentUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StudentUserMapper {

    @Select("""
            select id, phone, nickname, password, status, created_at, updated_at
            from student_user
            where phone = #{phone}
            limit 1
            """)
    StudentUser selectByPhone(String phone);

    @Select("select count(1) from student_user where status = 1")
    Long countActiveUsers();
}
