package com.zut.lpf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zut.common.utils.PageUtils;
import com.zut.lpf.entity.MessageEntity;
import com.zut.lpf.entity.UserEntity;
import com.zut.lpf.vo.UserVo;

import java.util.Map;

public interface UserService extends IService<UserEntity> {

    public void updateVisitTime(UserEntity userEntity) throws InterruptedException;

    UserVo getOthers(UserEntity loginEntity);

    PageUtils queryPage(Map<String, Object> params);


}
