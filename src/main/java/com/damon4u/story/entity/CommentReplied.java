package com.damon4u.story.entity;

import lombok.Data;

/**
 * Description: 评论中引用的评论信息
 *
 * @author damon4u
 * @version 2017-05-21 14:56
 */
@Data
public class CommentReplied {

    private User user;

    private String content;

    private int status;

}
