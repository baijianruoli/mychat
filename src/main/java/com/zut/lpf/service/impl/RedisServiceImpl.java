package com.zut.lpf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zut.lpf.dao.UserDao;
import com.zut.lpf.entity.UserEntity;
import com.zut.lpf.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RedisServiceImpl implements RedisService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserDao userDao;
    /**
     *
     * @param name
     * @return  好友list
     */
    public List<UserEntity> redisFindFriendList(String name)
    {
        List<UserEntity> list=new ArrayList<>();
        while(redisTemplate.boundListOps(name).size()>0)
        {
            Object o = redisTemplate.boundListOps(name).leftPop();
            UserEntity userList= (UserEntity) o;
            list.add(userList);
        }
        list.stream().forEach(res->{
            redisTemplate.boundListOps(name).rightPush(res);
        });

        return list;
    }

    @Override
    public void redisAddFriend(String friendName,String name) {

        UserEntity userEntity = userDao.selectOne(new QueryWrapper<UserEntity>().eq("name", friendName));
        userEntity.setFlag(1);
        redisTemplate.boundListOps(name).rightPush(userEntity);
    }

    @Override
    public void redisRemoveFriend(String friendName, String name) {
        List<UserEntity> list=new ArrayList<>();
        while(redisTemplate.boundListOps(name).size()>0)
        {
            Object o = redisTemplate.boundListOps(name).leftPop();
            UserEntity userList= (UserEntity) o;
            list.add(userList);
        }
        List<UserEntity> collect = list.stream().filter(res->
                !res.getName().equals(friendName)
                ).collect(Collectors.toList());
       collect.stream().forEach(res->{
            redisTemplate.boundListOps(name).rightPush(res);
        });
    }

    //判断name是否是friendName的好友
    @Override
    public boolean hashFrined(String friendName, String name) {

        List<UserEntity> list = redisFindFriendList(friendName);
        if(list.contains(name))
        {
            return true;
        }
        else
        {
            return false;
        }

    }
}
