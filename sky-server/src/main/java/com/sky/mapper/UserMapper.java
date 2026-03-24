package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select *from user where openid=#{openid}")
    @Options(useGeneratedKeys = true ,keyProperty = "id")
    public User selectByOpenid(String openid) ;
    @Insert("insert into user (openid,name,phone,sex,id_number,avatar) values (#{openid},#{name},#{phone},#{sex},#{idNumber},#{avatar})")
    void insert(User user);
}
