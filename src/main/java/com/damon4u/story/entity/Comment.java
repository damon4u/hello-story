package com.damon4u.story.entity;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * Description:
 *
 * @author damon4u
 * @version 2018-08-16 17:11
 */
@Data
public class Comment {

    private Long commentId;
    
    private Long songId;

    private Long likedCount;

    private String content;

    private Long userId;
    
    private Long beRepliedUserId;
    
    private String beRepliedContent;
    
    private Date commentTime;
    
    private Date createTime;
}
