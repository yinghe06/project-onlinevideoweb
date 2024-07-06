package com.imooc.bilibili;

import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.domain.User;
import com.imooc.bilibili.domain.UserInfo;
import com.imooc.bilibili.service.UserService;
import com.imooc.bilibili.support.UserSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class UserApi {
        @Autowired
        UserService userService;
        @Autowired
        UserSupport userSupport;
        @PostMapping("/user-tokens")
        public JsonResponse<String> login(@RequestBody User user) throws Exception {
                String token=userService.login(user);
                return new JsonResponse<>(token);
        }
        @PostMapping("/users")
        public JsonResponse<String> adduser(@RequestBody User user) {
               userService.addUser(user);
                return JsonResponse.success();
        }
        @GetMapping("/users")
        public JsonResponse<User> getUserInfo(){
                Long currentUserId = userSupport.getCurrentUserId();
                User userInfo = userService.getUserInfo(currentUserId);
                return new JsonResponse<>(userInfo);


        }
        @PutMapping("user-infos")
        public JsonResponse<String> updateUserInfos(@RequestBody UserInfo userInfo){
                Long userId = userSupport.getCurrentUserId();
                userInfo.setUserId(userId);
                userService.updateUserInfos(userInfo);
                return JsonResponse.success("成功");
        }
        @PostMapping("/user-dts")
        public JsonResponse<Map<String,Object>> loginForDts(@RequestBody User user) throws Exception {
                Map<String,Object> map=userService.loginForDts(user);
                return new JsonResponse<>(map);
        }
        @DeleteMapping("/access-tokens")
        public JsonResponse<String> logout(HttpServletRequest request){
                String refreshToken = request.getHeader("refreshToken");
                Long userId = userSupport.getCurrentUserId();
                userService.logout(refreshToken,userId);
                System.out.println(userId);
                return JsonResponse.success();
        }
        @PostMapping("/access-tokens")
        public JsonResponse<String> refreshAccessToken(HttpServletRequest request) throws Exception {
                String refreshToken = request.getHeader("refreshToken");
                String accessToken = userService.refreshAccessToken(refreshToken);
                return new JsonResponse<>(accessToken);
        }

}
