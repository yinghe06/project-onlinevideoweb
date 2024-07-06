package com.imooc.bilibili;

import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.domain.UserMoment;
import com.imooc.bilibili.domain.annotation.ApilimitedRole;
import com.imooc.bilibili.domain.constant.AuthRoleConstant;
import com.imooc.bilibili.service.UserMomentService;
import com.imooc.bilibili.support.UserSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserMomentApi {
    @Autowired
    UserSupport userSupport;
    @Autowired
    UserMomentService userMomentService;
    @ApilimitedRole(limitedRoleCodeList = {AuthRoleConstant.ROLE_LV1})
    @PostMapping("user-moments")
    public JsonResponse<String> addUserMoments(@RequestBody UserMoment userMoment) throws Exception {
        Long userId = userSupport.getCurrentUserId();
        userMoment.setUserId(userId);
        userMomentService.addUserMoments(userMoment);
        return JsonResponse.success("发送成功");
    }
    @GetMapping("/user-subscribed-moments")
    public JsonResponse<List<UserMoment>> getUserSubscribedMoments(){
        Long userId = userSupport.getCurrentUserId();
      List<UserMoment> list=  userMomentService.getUserSubscribedMoments(userId);
      return new JsonResponse<>(list);
    }

}
