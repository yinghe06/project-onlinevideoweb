package com.imooc.bilibili.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

@Mapper
public interface UserCoinDao {

    Integer getUserCoinsAmount(Long userId);

    void updateUserCoinsAmount(Long userId,Integer amount, Date updateTime);
}
