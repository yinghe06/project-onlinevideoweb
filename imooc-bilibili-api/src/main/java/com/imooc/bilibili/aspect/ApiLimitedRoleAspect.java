package com.imooc.bilibili.aspect;

import com.imooc.bilibili.domain.annotation.ApilimitedRole;
import com.imooc.bilibili.domain.auth.UserRole;
import com.imooc.bilibili.domain.exception.ConditionException;
import com.imooc.bilibili.service.UserRoleService;
import com.imooc.bilibili.support.UserSupport;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Order(1)
@Component
@Aspect
public class ApiLimitedRoleAspect {
    @Autowired
    private UserSupport userSupport;
    @Autowired
   private UserRoleService userRoleService;
    @Pointcut("@annotation(com.imooc.bilibili.domain.annotation.ApilimitedRole)")//切点会匹配方法上有ApilimitedRole的地方
    public void check(){

    }
    @Before("check()&&@annotation(apilimitedRole)")//只有当方法上有 @ApiLimitedRole 注解时,切点表达式才会匹配到,从而执行该 doBefore 方法。
    public void doBefore(JoinPoint joinPoint, ApilimitedRole apilimitedRole){
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        String[] limitedRoleCodeList = apilimitedRole.limitedRoleCodeList();
        Set<String> limitedRoleCodeSet = Arrays.stream(limitedRoleCodeList).collect(Collectors.toSet());
        Set<String> userRoleSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
        userRoleSet.retainAll(limitedRoleCodeSet);
        if (userRoleSet.size()>0){
            throw new ConditionException("权限不足");
        }

    }
}
