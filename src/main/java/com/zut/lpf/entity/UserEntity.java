package com.zut.lpf.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@TableName("user")
public class UserEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private int id;
    private String name;
    private String password;
    private String icon;
    @TableField(exist = false)
    private String remoteId;
    @TableField(exist = false)
    private List<UserEntity> friendList;
    @TableField(exist = false)
    private int friendFlag;

}
