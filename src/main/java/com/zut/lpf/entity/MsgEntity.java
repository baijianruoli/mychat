package com.zut.lpf.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@TableName("message")
public class MsgEntity implements Serializable {

    @TableId
    private String id;
    private int userId;
    private String msg;
    @TableField("user_icon")
    private String icon;
    @TableField("user_name")
    private String name;
    @TableField("create_time")
    private String time;
    @TableField(exist = false)
    private List<UserEntity> friendList;
    @TableField("accept_name")
    private String acceptId;
    @TableField(exist = false)
    private int friendFlag;

}
