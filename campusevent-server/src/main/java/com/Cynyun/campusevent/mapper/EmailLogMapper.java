package com.Cynyun.campusevent.mapper;

import com.Cynyun.campusevent.entity.EmailLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmailLogMapper {
    int insert(EmailLog log);
}
