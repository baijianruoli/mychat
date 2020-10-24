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
    public void redisAddFriend(String friedName,String name) {

        UserEntity userEntity = userDao.selectOne(new QueryWrapper<UserEntity>().eq("name", friedName));
        redisTemplate.boundListOps(name).rightPush(userEntity);
    }
}
