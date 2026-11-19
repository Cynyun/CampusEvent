package com.Cynyun.campusevent.mapper;

import com.Cynyun.campusevent.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {
    int insert(Message message);

    List<Message> findByStudent(@Param("studentId") String studentId);

    int markRead(@Param("id") Long id, @Param("studentId") String studentId);

    int markAllRead(@Param("studentId") String studentId);

    int countUnread(@Param("studentId") String studentId);
}