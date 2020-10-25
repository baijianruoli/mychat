package com.zut.lpf.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MsgEntity implements Serializable {

    private int id;
    private String msg;
    private String icon;
    private String name;
    private String time;
    private List<UserEntity> friendList;
    private String acceptId;
    private int friendFlag;

}
