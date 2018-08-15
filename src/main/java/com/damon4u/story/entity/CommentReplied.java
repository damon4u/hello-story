package com.damon4u.story.entity;

/**
 * Description: 评论中引用的评论信息
 *
 * @author damon4u
 * @version 2017-05-21 14:56
 */
public class CommentReplied {

    private User user;

    private String content;

    private int status;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "CommentReplied{" +
                "user=" + user +
                ", content='" + content + '\'' +
                ", status=" + status +
                '}';
    }
}
