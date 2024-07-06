/*
package com.imooc.bilibili.service;

import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.dao.FileDao;
import com.imooc.bilibili.domain.Danmu;
import com.imooc.bilibili.domain.File;
import com.imooc.bilibili.domain.constant.UserMomentsConstant;
import com.imooc.bilibili.service.config.WebSocketConfig;
import com.imooc.bilibili.service.util.FastdfsUtil;
import com.imooc.bilibili.service.util.MD5Util;

import com.imooc.bilibili.service.util.RocketMQUtil;
import com.imooc.bilibili.service.util.TokenUtil;
import io.netty.util.internal.StringUtil;
import org.apache.commons.codec.digest.DigestUtils;



import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;


@Component
@ServerEndpoint("/imserver/{token}")
public class WebSocektService {
    private final Logger logger =  LoggerFactory.getLogger(this.getClass());
    private Session session;
    private String sessionId;
    private Long userId;
    private static final AtomicInteger ONLINE_COUNT= new AtomicInteger(0);
    private  static ApplicationContext APPLICATION_CONTEXT;
   public static final ConcurrentHashMap<String,WebSocektService> WEBSOCKET_MAP=new ConcurrentHashMap<>();
    @OnOpen
   private void openConnection(Session session, @PathParam("token") String token){
        try{
            this.userId= TokenUtil.verifyToken(token);
        }catch (Exception e){

        }
    this.session=session;
    this.sessionId=session.getId();
    if (WEBSOCKET_MAP.contains(sessionId)){
        WEBSOCKET_MAP.remove(sessionId);
        WEBSOCKET_MAP.put(sessionId, this);//保证原子性，并发下不能直接使用put方法更新，可能导致不一致状态。先remove->put不会导致意外并发问题
    }else {
        WEBSOCKET_MAP.put(sessionId, this);
        ONLINE_COUNT.getAndIncrement();
    }
    logger.info("用户连接成功："+sessionId+"当前在线人数"+ONLINE_COUNT.get());
    try {
        this.sendMessage("0");
    }catch (Exception e){
        logger.error("连接异常");
    }
    }
    @OnClose
    public void closeConnection(){
        if(WEBSOCKET_MAP.containsKey(sessionId)){
            WEBSOCKET_MAP.remove(sessionId);
            ONLINE_COUNT.getAndDecrement();
        }
        logger.info("用户退出"+sessionId+"当前在线人数"+ONLINE_COUNT.get());
    }
    @OnMessage
    public void onMessage(String message){
        logger.info("用户信息："+sessionId+"，报文："+message);
        if (!StringUtil.isNullOrEmpty(message)) {
            try {
                for (Map.Entry<String, WebSocektService> entry : WEBSOCKET_MAP.entrySet()) {
                    WebSocektService webSocektService = entry.getValue();
                    DefaultMQProducer danmusProducer = (DefaultMQProducer) APPLICATION_CONTEXT.getBean("danmusProducer");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("message", message);
                    jsonObject.put("sessionId", webSocektService.getSessionId());
                    Message msg = new Message(UserMomentsConstant.TOPIC_DANMUS, jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8));
                    RocketMQUtil.asyncSendMsg(danmusProducer, msg);
                }
                //保存弹幕到数据库
                if (this.userId != null) {
                    Danmu danmu = JSONObject.parseObject(message, Danmu.class);
                    danmu.setUserId(userId);
                    danmu.setCreateTime(new Date());
                    DanmuService danmuService = (DanmuService) APPLICATION_CONTEXT.getBean("danmuService");
                    danmuService.addDanmu(danmu);
                    danmuService.addDanmusToRedis(danmu);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void sendMessage(String s) throws IOException {
        this.session.getBasicRemote().sendText(s);
    }
    public Session getSession() {
        return session;
    }
    //在线人数统计，设计时间间隔
    @Scheduled(fixedRate = 5000)
    private void noticeOnlineCount() throws IOException {
        for (Map.Entry<String,WebSocektService> entry:WebSocektService.WEBSOCKET_MAP.entrySet()){
            if(entry.getValue().session.isOpen()){
                //
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("onlineCount", ONLINE_COUNT.get());
                jsonObject.put("message", "当前在线人数"+ONLINE_COUNT.get());
                entry.getValue().sendMessage(jsonObject.toJSONString());
            }
        }
    }

    public String getSessionId() {
        return sessionId;
    }
}
*/
