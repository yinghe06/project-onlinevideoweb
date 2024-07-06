package com.imooc.bilibili.service;

import com.imooc.bilibili.dao.AuthRoleDao;
import com.imooc.bilibili.domain.auth.AuthRole;
import com.imooc.bilibili.domain.auth.AuthRoleElementOperation;
import com.imooc.bilibili.domain.auth.AuthRoleMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.relation.Role;
import java.util.List;
import java.util.Set;

@Service
public class AuthRoleService {
    @Autowired
    AuthRoleDao authRoleDao;
    @Autowired
    AuthRoleElementOperationService authRoleElementOperationService;
    @Autowired
    AuthRoleMenuService authRoleMenuService;
    public List<AuthRoleElementOperation> getRoleElementOperationByRoleIds(Set<Long> roleIdSet) {
        return authRoleElementOperationService.getRoleElementOperationByRoleIds(roleIdSet);
    }

    public List<AuthRoleMenu> getAuthRoleMenusByRoleIds(Set<Long> roleIdSet) {
    return authRoleMenuService.getAuthRoleMenusByRoleIds(roleIdSet);
    }


    public AuthRole getRoleByCode(String roleLv0) {
        return authRoleDao.getRoleByCode(roleLv0);
    }
}
