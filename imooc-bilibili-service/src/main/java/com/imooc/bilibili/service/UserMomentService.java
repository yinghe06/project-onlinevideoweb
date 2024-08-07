package com.imooc.bilibili.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.dao.UserMomentDao;
import com.imooc.bilibili.domain.UserMoment;
import com.imooc.bilibili.domain.constant.UserMomentsConstant;
import com.imooc.bilibili.service.util.RocketMQUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class UserMomentService {
    @Autowired
   private UserMomentDao userMomentDao;
    @Autowired
    private ApplicationContext applicationContext;//获取生产者和消费者配置
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    public void addUserMoments(UserMoment userMoment)throws Exception {
        userMoment.setCreateTime(new Date());
        userMomentDao.addUserMoments(userMoment);
        DefaultMQProducer producer = (DefaultMQProducer) applicationContext.getBean("momentsProducer");
        Message message = new Message(UserMomentsConstant.TOPIC_MOMENTS, JSONObject.toJSONString(userMoment).getBytes(StandardCharsets.UTF_8));
        RocketMQUtil.syncSendMsg(producer,message);
    }

    public List<UserMoment> getUserSubscribedMoments(Long userId) {
        String key="subscribed-"+userId;
        String listStr = redisTemplate.opsForValue().get(key);

        return JSONArray.parseArray(listStr, UserMoment.class);
    }
}
