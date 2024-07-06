package com.imooc.bilibili.dao;

import com.imooc.bilibili.domain.UserFollowing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFollowingDao {
    void deleteUserFollowing(@Param("userId") long userId, @Param("followingId") Integer followingId);

    void addUserFollowing(UserFollowing userFollowing);

    List<UserFollowing> getUserFollowings(Long userId);

    List<UserFollowing> getUserFans(Long userId);

    List<UserFollowing> checkFollowingStatus(Long userId);
}
