package com.damon4u.story.dao;

import com.damon4u.story.entity.Song;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

@Repository
public interface SongDao {

    @Insert("INSERT IGNORE INTO song (song_id, name, description, comment_count, image, create_time) " +
            "VALUES (#{songId}, #{name}, #{description}, #{commentCount}, #{image}, #{createTime})")
    int save(Song song);

}
