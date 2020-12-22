package com.zut.lpf.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zut.common.utils.PageUtils;
import com.zut.common.utils.Query;
import com.zut.lpf.dao.UserDao;
import com.zut.lpf.entity.MessageEntity;
import com.zut.lpf.entity.UserEntity;
import com.zut.lpf.service.MsgService;
import com.zut.lpf.service.UserService;
import com.zut.lpf.vo.UserVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserDao, UserEntity> implements UserService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private MsgService msgService;

    @Async
    @Override
    public void updateVisitTime(UserEntity userEntity) throws InterruptedException {
        userEntity.setLastVisit(new Date());
        userDao.updateById(userEntity);
    }

    @Override
    public UserVo getOthers(UserEntity loginEntity) {
        UserVo userVo = new UserVo();
        HashMap<String, List<MessageEntity>> hashMap = new HashMap();
        BeanUtils.copyProperties(loginEntity, userVo);
        List<UserEntity> friendList = loginEntity.getFriendList();
        friendList.forEach(res -> {
            List<MessageEntity> list = msgService.getList(loginEntity.getName(), res.getName(), 1);
            hashMap.put(res.getName(), list);
        });
        List<MessageEntity> all = msgService.getAll(1);
        hashMap.put("all", all);
        userVo.setOther(hashMap);
        return userVo;
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        IPage<UserEntity> page = this.page(
                new Query<UserEntity>().getPage(params),
                new QueryWrapper<UserEntity>().like("name", key)
        );

        return new PageUtils(page);
    }


}
