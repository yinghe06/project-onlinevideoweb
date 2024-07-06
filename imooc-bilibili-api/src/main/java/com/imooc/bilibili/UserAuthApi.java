package com.imooc.bilibili;

import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.domain.auth.UserAuthorities;
import com.imooc.bilibili.service.UserAuthService;
import com.imooc.bilibili.support.UserSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserAuthApi {
    @Autowired
    UserSupport userSupport;
    @Autowired
    UserAuthService userAuthService;
    @GetMapping("/user-authorities")
    public JsonResponse<UserAuthorities> getUserAuthorities(){
        Long userId = userSupport.getCurrentUserId();
        UserAuthorities userAuthorities = userAuthService.getUserAuthorities(userId);
        return  new JsonResponse<>(userAuthorities);
    }
}
