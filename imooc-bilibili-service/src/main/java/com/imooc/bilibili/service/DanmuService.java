package com.imooc.bilibili.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.dao.DanmuDao;
import com.imooc.bilibili.dao.DemoDao;
import com.imooc.bilibili.domain.Danmu;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class DanmuService {

    RedisTemplate<String,String> redisTemplate;
    private static final String DANMU_KEY = "dm-video-";
    @Autowired
    DanmuDao danmuDao;
    public void addDanmu(Danmu danmu) {

    danmuDao.addDanmu(danmu);
    }
    @Async
    public void asyncAddDanmu(Danmu danmu){
        danmuDao.addDanmu(danmu);
    }
    public List<Danmu> getDanmus(Long videoId,String start,String end) throws Exception{
        String key="danmu-video-"+videoId;
        String value = redisTemplate.opsForValue().get(key);
        List<Danmu> list = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(value)) {
            list = JSONArray.parseArray(value, Danmu.class);
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(end)) {

                SimpleDateFormat sdf = new SimpleDateFormat();
                Date startime = sdf.parse(start);
                Date endtime = sdf.parse(end);
                List<Danmu> childlist=new ArrayList<>();
                for (Danmu danmu:list){
                    Date createTime = danmu.getCreateTime();
                    if(createTime.before(endtime)&&createTime.after(startime)){
                        childlist.add(danmu);
                    }
                }
                list=childlist;
            }

        }else{
            HashMap<String, Object> params = new HashMap<>();
            params.put("videoId", videoId);
            params.put("startTime", start);
            params.put("endTime", end);
           list= danmuDao.getDanmus(params);
           redisTemplate.opsForValue().set(key, JSONObject.toJSONString(list));
        }
        return list;

    }

    public void addDanmusToRedis(Danmu danmu) {

        String key="danmu-video-"+danmu.getVideoId();
        String value = redisTemplate.opsForValue().get(key);
        List<Danmu> list = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(value)) {
         list = JSONArray.parseArray(value, Danmu.class);
        }
        list.add(danmu);
      redisTemplate.opsForValue().set(key, JSONObject.toJSONString(list));

    }
}
