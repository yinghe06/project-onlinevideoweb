package com.imooc.bilibili.dao;

import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.domain.RefreshTokenDetail;
import com.imooc.bilibili.domain.User;
import com.imooc.bilibili.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface UserDao {
    void addUserInfo(UserInfo userInfo);

    User getUserByPhone(String phone);

    void addUser(User user);

    User getUserById(Long userId);

    UserInfo getUserInfoById(Long userId);

    void updateUserInfos(UserInfo userInfo);

    List<UserInfo> getUserInfoByUserIds();


    Integer pageCountUserInfos(JSONObject param);

    List<UserInfo> pageListUserInfos(JSONObject param);

    void deleteRefreshToken(@Param("refreshToken") String refreshToken, @Param("userId") Long userId);

    void addRefreshToken(@Param("refreshToken") String refreshToken, @Param("userId") Long userId, @Param("createTime") Date createTime);


    RefreshTokenDetail getRefreshTokenDetail(String refreshToken);
}
