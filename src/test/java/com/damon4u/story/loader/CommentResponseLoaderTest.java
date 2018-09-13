package com.damon4u.story.loader;

import com.damon4u.story.dao.UserDao;
import com.damon4u.story.entity.Song;
import com.damon4u.story.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Description:
 *
 * @author damon4u
 * @version 2017-05-21 16:14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class CommentResponseLoaderTest {

    @Resource
    private CommentLoader commentLoader;
    
    @Resource
    private UserDao userDao;

    @Test
    public void loadSongs() throws Exception {
        commentLoader.loadSongs(210032,220000);
    }

    @Test
    public void testUser() {
        User user = new User();
        user.setNickname("我是否收到");
        user.setUserId(1233L);
        user.setAvatarUrl("asdf");
        user.setCreateTime(new Date());
        userDao.save(user);
    }

}