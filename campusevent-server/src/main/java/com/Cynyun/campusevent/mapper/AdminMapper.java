package com.Cynyun.campusevent.mapper;

import com.Cynyun.campusevent.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminMapper {
    Admin findByAdminId(@Param("adminId") String adminId);
}