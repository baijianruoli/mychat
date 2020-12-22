package com.zut.lpf.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@TableName("message")
public class MessageEntity {
    @TableId
    private String id;
    private int userId;
    private String msg;
    @TableField("user_icon")
    private String icon;
    @TableField("user_name")
    private String name;
    @TableField("create_time")
    private Date time;
    @TableField("accept_name")
    private String acceptId;
}
