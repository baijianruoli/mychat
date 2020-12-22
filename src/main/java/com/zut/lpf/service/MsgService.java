package com.zut.lpf.service;

import com.zut.lpf.entity.MessageEntity;
import com.zut.lpf.entity.MsgEntity;

import java.util.List;

public interface MsgService {



    public List<MessageEntity> getList(String name, String FriendName, int current);

    public List<MessageEntity> getAll(int current);

    public void deleteFriendMsg(String name, String friendName);

    public void loadMessage(MessageEntity message);
}
