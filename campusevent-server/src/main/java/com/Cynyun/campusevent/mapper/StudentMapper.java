package com.Cynyun.campusevent.mapper;

import com.Cynyun.campusevent.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StudentMapper {
    Student findByStudentId(@Param("studentId") String studentId);
}