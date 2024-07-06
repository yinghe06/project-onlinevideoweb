package com.imooc.bilibili.service;

import com.imooc.bilibili.dao.UserDao;
import com.imooc.bilibili.domain.RefreshTokenDetail;
import com.imooc.bilibili.domain.User;
import com.imooc.bilibili.domain.UserInfo;
import com.imooc.bilibili.domain.constant.UserConstant;
import com.imooc.bilibili.domain.exception.ConditionException;
import com.imooc.bilibili.service.util.MD5Util;
import com.imooc.bilibili.service.util.RSAUtil;
import com.imooc.bilibili.service.util.TokenUtil;
import com.mysql.cj.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    @Autowired
    UserDao userDao;
    @Autowired
    UserAuthService userAuthService;


    public void addUser(User user){
        String phone = user.getPhone();
        if(StringUtils.isNullOrEmpty(phone)){
            throw new ConditionException("手机号不为空");
        }
       User dbUser =this.getUserByPhone(phone);
        if (dbUser!=null){
            throw new ConditionException("该用户已存在");
        }
        Date now = new Date();
        String salt = String.valueOf(now.getTime());
        String password = user.getPassword();
        String rawpassword;
        try {
            rawpassword= RSAUtil.decrypt(password);
        }catch (Exception e){
            throw new ConditionException("解密失败");
        }
        String passwordmd5 = MD5Util.sign(rawpassword, salt, "utf-8");
        user.setSalt(salt);
        user.setPassword(passwordmd5);
        user.setCreatetime(now);
        userDao.addUser(user);
        //添加用户信息
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setCreatetime(user.getCreatetime());
        userInfo.setBirth(UserConstant.DEFAULT_BIRTH);
        userInfo.setGender(UserConstant.GENDER_MALE);
        userInfo.setNick(UserConstant.DEFAULT_NICK);
        userDao.addUserInfo(userInfo);
        //添加用户默认权限角色
        userAuthService.addUserDefaultRole(user.getId());
    }
    public String login(User user) throws Exception {
        String phone = user.getPhone();
        if(StringUtils.isNullOrEmpty(phone)){
            throw new ConditionException("手机号不为空");
        }
        User dbUser =this.getUserByPhone(phone);
        if (dbUser==null){
            throw new ConditionException("该用户不存在");
        }
        String password = user.getPassword();
        String rawpassword;
        try {
            rawpassword= RSAUtil.decrypt(password);
        }catch (Exception e){
            throw new ConditionException("解密失败");
        }
        String salt = dbUser.getSalt();
        String MD5 = MD5Util.sign(rawpassword, salt, "utf-8");
        if (!MD5.equals(dbUser.getPassword())){
            throw new ConditionException("密码不正确");
        }
        return TokenUtil.generateToken(dbUser.getId());
    }


    private User getUserByPhone(String phone) {
        return userDao.getUserByPhone(phone);
    }

    public User getUserInfo(Long userId){
        User user = userDao.getUserById(userId);
        UserInfo userInfo = userDao.getUserInfoById(userId);
        user.setUserInfo(userInfo);
        return user;
    }
    public void updateUserInfos(UserInfo userInfo){
        userInfo.setUpdatetime(new Date());
        userDao.updateUserInfos(userInfo);
    }

    public User getUserById(Integer followingId) {
      return  userDao.getUserById(Long.valueOf(followingId));
    }

    public List<UserInfo> getUserInfoByUserIds(Set<Long> followingIdSet) {
     return    userDao.getUserInfoByUserIds();
    }

    public Map<String, Object> loginForDts(User user) throws Exception {
        String phone = user.getPhone();
        if(StringUtils.isNullOrEmpty(phone)){
            throw new ConditionException("手机号不为空");
        }
        User dbUser =this.getUserByPhone(phone);
        if (dbUser==null){
            throw new ConditionException("该用户不存在");
        }
        String password = user.getPassword();
        String rawpassword;
        try {
            rawpassword= RSAUtil.decrypt(password);
        }catch (Exception e){
            throw new ConditionException("解密失败");
        }
        String salt = dbUser.getSalt();
        String MD5 = MD5Util.sign(rawpassword, salt, "utf-8");
        if (!MD5.equals(dbUser.getPassword())){
            throw new ConditionException("密码不正确");
        }
        Long userId = dbUser.getId();
        String accessToken = TokenUtil.generateToken(userId);
        String refreshToken = TokenUtil.generateRefreshToken(userId);
        userDao.deleteRefreshToken(refreshToken,userId);
        userDao.addRefreshToken(refreshToken,userId,new Date());
        HashMap<String, Object> result = new HashMap<>();
        result.put("accessToken",accessToken);
        result.put("refreshToken",refreshToken);
        return result;

    }

    public void logout(String refreshToken, Long userId) {
        userDao.deleteRefreshToken(refreshToken, userId);
    }

    public String refreshAccessToken(String refreshToken) throws Exception {
        RefreshTokenDetail refreshTokenDetail= userDao.getRefreshTokenDetail(refreshToken);
        if (refreshTokenDetail == null) {
            throw new ConditionException("555","token过期");

        }
      Long userId=  refreshTokenDetail.getUserId();
        return TokenUtil.generateToken(userId);
    }

    public List<UserInfo> batchGetUserInfoById(Set<Long> userIdList) {
        List<UserInfo> userInfoList = new ArrayList<>();
        userIdList.forEach(userId->{
           UserInfo userInfo= userDao.getUserInfoById(userId);
           userInfoList.add(userInfo);
        });
        return userInfoList;
    }
}
