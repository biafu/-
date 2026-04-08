package com.zhinengpt.campuscatering.message.mapper;

import com.zhinengpt.campuscatering.message.entity.StudentMessage;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StudentMessageMapper {

    @Insert("""
            insert into student_message(user_id, title, content, message_type, biz_type, biz_id, is_read, created_at, updated_at)
            values(#{userId}, #{title}, #{content}, #{messageType}, #{bizType}, #{bizId}, #{isRead}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StudentMessage studentMessage);

    @Select("""
            select id, user_id, title, content, message_type, biz_type, biz_id, is_read, created_at, updated_at
            from student_message
            where user_id = #{userId}
              and (#{onlyUnread} = 0 or is_read = 0)
            order by id desc
            limit #{limit}
            """)
    List<StudentMessage> selectLatest(@Param("userId") Long userId, @Param("onlyUnread") Integer onlyUnread, @Param("limit") Integer limit);

    @Update("""
            update student_message
            set is_read = 1, updated_at = now()
            where id = #{id} and user_id = #{userId}
            """)
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("""
            update student_message
            set is_read = 1, updated_at = now()
            where user_id = #{userId} and is_read = 0
            """)
    int markAllRead(Long userId);

    @Select("""
            select count(1)
            from student_message
            where user_id = #{userId} and is_read = 0
            """)
    Long countUnread(Long userId);
}
