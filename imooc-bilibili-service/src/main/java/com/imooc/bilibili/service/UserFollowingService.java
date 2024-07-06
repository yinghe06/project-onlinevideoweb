package com.imooc.bilibili.service;

import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.dao.FollowingGroupDao;
import com.imooc.bilibili.dao.UserDao;
import com.imooc.bilibili.dao.UserFollowingDao;
import com.imooc.bilibili.domain.*;
import com.imooc.bilibili.domain.constant.UserConstant;
import com.imooc.bilibili.domain.exception.ConditionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserFollowingService {
    @Autowired
    FollowingGroupService followingGroupService;
    @Autowired
    UserService userService;
    @Autowired
    UserFollowingDao userFollowingDao;
    @Autowired
    UserDao userDao;
    @Transactional
    public void addUserFollowings(UserFollowing userFollowing) {
        System.out.println(userFollowing.getUserid());
        Integer groupId = userFollowing.getGroupId();
        if (groupId == null) {
            FollowingGroup followingGroup = followingGroupService.getByType(UserConstant.USER_FOLLOWING_GROUP_TYPE_DEFAULT);
            userFollowing.setGroupId(Math.toIntExact(followingGroup.getId()));
        } else {
            FollowingGroup followingGroup = followingGroupService.getById(Long.valueOf(groupId));
            if (followingGroup == null) {
                throw new ConditionException("关注分组不存在");
            }
        }
        Integer followingId = userFollowing.getFollowingId();
        User user = userService.getUserById(followingId);
        if (user == null) {
            throw new ConditionException("用户不存在");
        }
        //userFollowingDao.deleteUserFollowing(userFollowing.getUserid(), followingId);
        userFollowing.setCreateTime(new Date());
        System.out.println(userFollowing.getUserid());
        userFollowingDao.addUserFollowing(userFollowing);
    }
    //获取关注用户list,获取每个关注用户端id，并使用set对id去重
    //批量获取关注用户信息
    //将关注用户按照groupid分组，要记得先通过通过userid获取groupidlist，然后在对关注用户进行分组

    public List<FollowingGroup> getUserFollowings(Long userId){
        List<UserFollowing> list = userFollowingDao.getUserFollowings(userId);
        Set<Long> followingIdSet = list.stream()
                .mapToLong(UserFollowing::getFollowingId)  // 直接映射为 long
                .boxed()                                    // 装箱转换为 Long
                .collect(Collectors.toSet());
//UserFollowing 对象的列表中,提取出所有 followingId 的值,并去重后返回一个 Set 集合
        List<UserInfo> userInfoList = new ArrayList<>();
        if(followingIdSet.size()>0){
            userInfoList = userService.getUserInfoByUserIds(followingIdSet);
        }
        for(UserFollowing userFollowing:list){
            for (UserInfo userInfo:userInfoList){
                if (userFollowing.getUserid().equals(userInfo.getUserId())){
                    userFollowing.setUserInfo(userInfo);//给关注用户增加详细用户信息
                }
            }
        }

        List<FollowingGroup> followingGroupList = followingGroupService.getByUserId(userId);
        List<FollowingGroup> result = new ArrayList<>();
        for (FollowingGroup group:followingGroupList){
            List<UserInfo> infoList = new ArrayList<>();
            for(UserFollowing userFollowing:list){
                if (group.getId().equals(userFollowing.getGroupId())){
                    infoList.add(userFollowing.getUserInfo());
                }
            }
            group.setFollowingUserInfoList(infoList);
            result.add(group);
        }
        return result;

    }
    //1.根据uid查询粉丝列表，2.根据粉丝id批量查询粉丝用户信息 3.查询当前用户是否关注该粉丝
    public List<UserFollowing> getUserFans(Long userId){
        List<UserFollowing> fanList=  userFollowingDao.getUserFans(userId);
        Set<Long> fanIdSet = fanList.stream().map(UserFollowing::getUserid).collect(Collectors.toSet());
        List<UserInfo> userInfoList=new ArrayList<>();
        if(fanIdSet.size()>0){
           userInfoList= userService.getUserInfoByUserIds(fanIdSet);
        }
        List<UserFollowing> userFollowings = userFollowingDao.getUserFollowings(userId);
        for(UserFollowing fan:fanList){
            for(UserInfo userInfo:userInfoList){
                if(fan.getUserid().equals(userInfo.getUserId())){
                    userInfo.setFollowed(false);
                    fan.setUserInfo(userInfo);
                }
            }
            for (UserFollowing userFollowing:userFollowings){
                if(userFollowing.getUserid().equals(fan.getUserid())){
                    fan.getUserInfo().setFollowed(true);
                }
            }
        }
    return fanList;
    }


    public Long addUserFollowingsGroups(FollowingGroup followingGroup) {
        followingGroup.setCreateTime(new Date());
        followingGroup.setType(UserConstant.USER_FOLLOWING_GROUP_TYPE_USER);
        followingGroupService.addUserFollowingsGroup(followingGroup);
        return followingGroup.getId()  ;
    }

    public List<FollowingGroup> getUserFollowingGroups(Long userId) {
        return followingGroupService.getUserFollowingGroups(userId);
    }

    public PageResult<UserInfo> pageListUserInfos(JSONObject param) {
        Integer no = param.getInteger("no");
        Integer size = param.getInteger("size");
        param.put("start", (no-1)*size);
        param.put("limit", size);
        Integer total=userDao.pageCountUserInfos(param);
        List<UserInfo> list=new ArrayList<>();
        if(total>0){
            list=userDao.pageListUserInfos(param);
        }
        return new PageResult<>(total,list);
    }

    public List<UserInfo> checkFollowingStatus(List<UserInfo> result, Long userId) {
      List<UserFollowing> userFollowingList= userFollowingDao.checkFollowingStatus(userId);
      for(UserInfo userInfo:result){
          userInfo.setFollowed(false);
          for (UserFollowing userFollowing:userFollowingList){
              if (userFollowing.getFollowingId().equals(userInfo.getUserId())){
                  userInfo.setFollowed(true);
              }
          }
      }
      return result;
    }
}
