package com.zhinengpt.campuscatering.auth.mapper;

import com.zhinengpt.campuscatering.auth.entity.SysAdmin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysAdminMapper {

    @Select("""
            select id, username, password, real_name, status, created_at, updated_at
            from sys_admin
            where username = #{username}
            limit 1
            """)
    SysAdmin selectByUsername(String username);
}
