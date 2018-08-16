package com.damon4u.story.entity;

import lombok.Data;

import java.util.List;

/**
 * Description: 评论信息
 *
 * @author damon4u
 * @version 2017-05-21 14:49
 */
@Data
public class CommentResponse {

    private long commentId;

    private long likedCount;

    private long time;

    private String content;

    private User user;

    private List<CommentReplied> beReplied;

}
