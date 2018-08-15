package com.damon4u.story.loader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * Description:
 *
 * @author damon4u
 * @version 2017-05-21 16:14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext.xml")
public class CommentLoaderTest {

    @Resource
    private CommentLoader commentLoader;

    @Test
    public void loadSongs() throws Exception {
        commentLoader.loadSongs(32317208,32317208);
    }

    @Test
    public void testSongInfo() {
        String songInfo = commentLoader.getSongInfo(32317208);
        System.out.println(songInfo);
    }

}