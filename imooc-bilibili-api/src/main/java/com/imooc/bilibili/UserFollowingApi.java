package com.imooc.bilibili;

import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.domain.*;
import com.imooc.bilibili.service.UserFollowingService;
import com.imooc.bilibili.service.UserService;
import com.imooc.bilibili.support.UserSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.naming.ldap.PagedResultsControl;
import java.util.List;

@RestController
public class UserFollowingApi {
    @Autowired
    UserFollowingService userFollowingService;
    @Autowired
    UserSupport userSupport;
    @Autowired
    UserService userService;
    @PostMapping("/userFollowing")
    public void addUserFollowings(@RequestBody UserFollowing userFollowing) {
        Long userId = userSupport.getCurrentUserId();
        userFollowing.setUserid(userId);
        System.out.println(userFollowing.getUserid());
        userFollowingService.addUserFollowings(userFollowing);
    }
    @GetMapping("/user-followings")
    public JsonResponse<List<FollowingGroup>> getUserFollowings(){
        Long userId = userSupport.getCurrentUserId();
        List<FollowingGroup> userFollowings = userFollowingService.getUserFollowings(userId);
        return new JsonResponse<>(userFollowings);

    }
    @GetMapping("/user-fans")
    public JsonResponse<List<UserFollowing>> getUserFans(){
        Long userId = userSupport.getCurrentUserId();//数据库字段值更新，token需要重新获取，猜想token本身包含了所有字段的信息
        System.out.println(userId);
        List<UserFollowing> userFans = userFollowingService.getUserFans(userId);
        return new JsonResponse<>(userFans);
    }
    @PostMapping("/user-following-groups")
    public JsonResponse<Long> addUserFollowingGroup(@RequestBody FollowingGroup followingGroup){
        Long userId = userSupport.getCurrentUserId();
        followingGroup.setUserId(userId);
     Long grouId=  userFollowingService.addUserFollowingsGroups(followingGroup);
        return new JsonResponse<>(grouId);
    }
    @GetMapping("/user-following-groups")
    public JsonResponse<List<FollowingGroup>> getUserFollowingGroup() {
        Long userId = userSupport.getCurrentUserId();
       List<FollowingGroup> list= userFollowingService.getUserFollowingGroups(userId);
       return new JsonResponse<>(list);
    }
    @GetMapping("/user-infos")
    public JsonResponse<PageResult<UserInfo>> pageListUserInfo(@RequestParam(defaultValue = "1") Integer no, // 默认页码为1
                                                               @RequestParam(defaultValue = "5") Integer size, // 默认每页10条数据
                                                               @RequestParam String nick){
        Long userId = userSupport.getCurrentUserId();
        JSONObject param = new JSONObject();
        param.put("no", no);
        param.put("size", size);
        param.put("nick", nick);
        param.put("userId", userId);
        PageResult<UserInfo> result=userFollowingService.pageListUserInfos(param);
        if (result.getTotal()>0){
            //查询用户表关注状态
            List<UserInfo> checkedUserInfoList=userFollowingService.checkFollowingStatus(result.getList(),userId);
            result.setList(checkedUserInfoList);
        }
        return new JsonResponse<>(result);
    }


}
