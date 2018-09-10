package com.damon4u.story.loader;

import org.apache.http.HttpHost;
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
 * @version 2018-08-30 20:35
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class ProxyLoaderTest {
        
    @Resource
    private ProxyLoader proxyLoader;
    
    @Test
    public void loadFromGit() {
        proxyLoader.loadFromGit();
    }
    
    @Test
    public void testValidate() {
        assertTrue(proxyLoader.validateProxy(new HttpHost("66.70.222.225", 80, "http")));
    }

    @Test
    public void validateFromDB() {
        proxyLoader.validateFromDb();
    }

}