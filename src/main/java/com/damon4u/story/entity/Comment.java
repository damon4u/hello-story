package com.damon4u.story.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.List;

/**
 * Description: 评论信息
 *
 * @author damon4u
 * @version 2017-05-21 14:49
 */
public class Comment {

    private long commentId;

    private long likedCount;

    private long time;

    private String content;

    private User user;

    private List<CommentReplied> beReplied;

    @JsonSerialize(using = ToStringSerializer.class)
    public long getCommentId() {
        return commentId;
    }

    public void setCommentId(long commentId) {
        this.commentId = commentId;
    }

    public long getLikedCount() {
        return likedCount;
    }

    public void setLikedCount(long likedCount) {
        this.likedCount = likedCount;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<CommentReplied> getBeReplied() {
        return beReplied;
    }

    public void setBeReplied(List<CommentReplied> beReplied) {
        this.beReplied = beReplied;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "commentId=" + commentId +
                ", likedCount=" + likedCount +
                ", time=" + time +
                ", content='" + content + '\'' +
                ", user=" + user +
                ", beReplied=" + beReplied +
                '}';
    }
}
