package com.zut.lpf.vo;

import com.zut.lpf.entity.UserEntity;
import lombok.Data;

@Data
public class UserVo<T> extends UserEntity {

    public T other;
}
