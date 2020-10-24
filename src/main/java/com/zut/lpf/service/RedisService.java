package com.zut.lpf.service;

import com.zut.lpf.entity.UserEntity;

import java.util.List;

public interface RedisService {
    public List<UserEntity> redisFindFriendList(String name);
    public void redisAddFriend(String frinedName,String name);
}
