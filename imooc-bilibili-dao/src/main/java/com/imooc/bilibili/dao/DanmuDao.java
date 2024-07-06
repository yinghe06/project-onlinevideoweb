package com.imooc.bilibili.dao;

import com.imooc.bilibili.domain.Danmu;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;


@Mapper
public interface DanmuDao {

    void addDanmu(Danmu danmu);

    List<Danmu> getDanmus(HashMap<String, Object> params);
}
