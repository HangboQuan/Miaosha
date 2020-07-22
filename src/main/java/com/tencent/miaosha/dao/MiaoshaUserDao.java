package com.tencent.miaosha.dao;

import com.tencent.miaosha.domain.MiaoshaUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MiaoshaUserDao {

    @Select("select * from miaosha_user where id = #{id}")
    public MiaoshaUser getMiaoshaUserById(@Param("id") long id);

    @Update("update miaosha_user set password = #{password} where id = #{id}")
    void update(MiaoshaUser miaoshaUser1);
}
