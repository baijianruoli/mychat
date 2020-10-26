package com.zut.lpf.controller;

import com.zut.lpf.entity.UserEntity;
import com.zut.lpf.response.BaseResponse;
import com.zut.lpf.response.StatusCode;
import com.zut.lpf.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RedisController {

    @Autowired
    private RedisService redisService;

    //添加好友
    @RequestMapping("/addFriend")
    public BaseResponse addFriend(String friendName, String name)
    {
        redisService.redisAddFriend(friendName,name);
        return new BaseResponse(StatusCode.Success);
    }
    //获取好友列表
    @RequestMapping("/getFriendList")
    public BaseResponse addFriend( String name)
    {

        List<UserEntity> list = redisService.redisFindFriendList(name);
        BaseResponse baseResponse = new BaseResponse(StatusCode.Success);
        baseResponse.setData(list);
        return baseResponse;
    }
    //删除好友
    @RequestMapping("/removeFriend")
    public BaseResponse removeFriend(String name,String friendName)
    {
        redisService.redisRemoveFriend(friendName,name);
        return new BaseResponse(StatusCode.Success);
    }
}
