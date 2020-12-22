package com.zut.lpf.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@TableName("user")
@ApiModel("用户对象")
public class UserEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(name = "用户id")
    private int id;
    @ApiModelProperty(name = "用户姓名")
    private String name;
    @ApiModelProperty(name = "用户密码")
    private String password;
    @ApiModelProperty(name = "用户图像")
    private String icon;
    @ApiModelProperty(name = "用户创建日期")
    private Date createTime;
    @ApiModelProperty(name = "用户最后修改日期")
    private Date lastVisit;
    @ApiModelProperty(required = false)
    @TableField(exist = false)
    private String remoteId;
    @TableField(exist = false)
    @ApiModelProperty(required = false)
    private List<UserEntity> friendList;
    @TableField(exist = false)
    @ApiModelProperty(required = false)
    private int friendFlag;

}
