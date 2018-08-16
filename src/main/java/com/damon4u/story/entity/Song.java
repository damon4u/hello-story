package com.damon4u.story.entity;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * Description:
 *
 * @author damon4u
 * @version 2018-08-16 17:29
 */
@Data
public class Song {
    
    private Long songId;
    
    private String name;
    
    private String description;
    
    private String image;
    
    private Date createTime;

}
