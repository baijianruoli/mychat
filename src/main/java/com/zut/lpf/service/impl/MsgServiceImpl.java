package com.zut.lpf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zut.lpf.dao.MessageDao;
import com.zut.lpf.entity.MessageEntity;
import com.zut.lpf.entity.MsgEntity;
import com.zut.lpf.entity.UserEntity;
import com.zut.lpf.service.MsgService;
import com.zut.lpf.util.GlobalLock;
import com.zut.lpf.util.SnowFlakeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MsgServiceImpl implements MsgService {

    @Autowired
    private MessageDao messageDao;
    @Autowired
    private SnowFlakeUtils snowFlakeUtils;
    @Async
    public void loadMessage(MessageEntity message)
    {
        message.setId(String.valueOf(snowFlakeUtils.nextId()));
        messageDao.insert(message);
    }

    @Override
    public List<MessageEntity> getList(String name, String FriendName, int current) {
        QueryWrapper<MessageEntity> queryWrapper = new QueryWrapper<>();
        Page page = new Page(current, 30);
        queryWrapper.and(res -> res.eq("user_name", name).eq("accept_name", FriendName)).or(res -> res.eq("user_name", FriendName).eq("accept_name", name)).orderByDesc("create_time");
        IPage pages = messageDao.selectPage(page, queryWrapper);
        List records = pages.getRecords();
        return records;
    }

    @Override
    public List<MessageEntity> getAll(int current) {
        QueryWrapper<MessageEntity> queryWrapper = new QueryWrapper<>();
        Page page = new Page(current, 30);
        queryWrapper.eq("accept_name", "all").orderByDesc("create_time");
        IPage pages = messageDao.selectPage(page, queryWrapper);
        List records = pages.getRecords();
        return records;
    }

    @Async
    @Override
    public void deleteFriendMsg(String name, String friendName) {
        QueryWrapper<MessageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(res -> res.eq("user_name", name).eq("accept_name", friendName));
        messageDao.delete(queryWrapper);
    }


}
