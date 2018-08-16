package com.damon4u.story.entity;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * Description: 用户信息
 *
 * @author damon4u
 * @version 2017-05-21 14:49
 */
@Data
public class User {

    private Long userId;

    private String nickname;

    private String avatarUrl;
    
    private Date createTime;

}
