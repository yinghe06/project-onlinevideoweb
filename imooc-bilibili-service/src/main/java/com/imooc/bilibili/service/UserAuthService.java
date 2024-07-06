package com.imooc.bilibili.service;

import com.imooc.bilibili.domain.UserInfo;
import com.imooc.bilibili.domain.auth.*;
import com.imooc.bilibili.domain.constant.AuthRoleConstant;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.relation.Role;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserAuthService {
    @Autowired
    UserRoleService userRoleService;
    @Autowired
    AuthRoleService authRoleService;
    @Autowired
    UserAuthorities userAuthorities;
    public UserAuthorities getUserAuthorities(Long userId) {
      List<UserRole> userRoleList= userRoleService.getUserRoleByUserId(userId);
        Set<Long> roleIdSet = userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
       List<AuthRoleElementOperation> authRoleElementOperationList= authRoleService.getRoleElementOperationByRoleIds(roleIdSet);
        List<AuthRoleMenu> authRoleMenuList =authRoleService.getAuthRoleMenusByRoleIds(roleIdSet);
        userAuthorities.setRoleMenuList(authRoleMenuList);
        userAuthorities.setRoleElementOperationList(authRoleElementOperationList);
        return  userAuthorities;

    }

    public void addUserDefaultRole(Long userId) {
        UserRole userRole = new UserRole();
        AuthRole role= authRoleService.getRoleByCode(AuthRoleConstant.ROLE_LV0);
        userRole.setRoleId( role.getId());
        userRole.setUserId(userId);
        userRoleService.addUserRole(userRole);
    }
}
