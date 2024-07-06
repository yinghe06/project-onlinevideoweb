package com.imooc.bilibili.dao;

import com.imooc.bilibili.domain.auth.AuthRole;
import com.imooc.bilibili.domain.auth.AuthRoleElementOperation;
import com.imooc.bilibili.domain.auth.AuthRoleMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

@Mapper
public interface AuthRoleDao {

    AuthRole getRoleByCode(String roleLv0);
}
