package com.damon4u.story.dao;

import com.damon4u.story.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

@Repository
public interface ProxyDao {

    @Insert("INSERT IGNORE INTO user (user_id, nickname, avatar_url, create_time) " +
            "VALUES (#{userId}, #{nickname}, #{avatarUrl}, #{createTime})")
    int save(User user);

}
