package com.imooc.bilibili.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.bind.annotation.RestController;


@Mapper
public interface DemoDao {
    public int  query(int id);
}
