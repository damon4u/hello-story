package com.damon4u.story.job;

import com.damon4u.story.loader.ProxyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * Description:
 *
 * @author damon4u
 * @version 2018-09-10 11:57
 */
public class ProxyLoadJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyLoadJob.class);
    
    @Resource
    private ProxyLoader proxyLoader;
    
    public void execute() {
        LOGGER.info("ProxyLoadJob start...");
        proxyLoader.loadFromGit();
        LOGGER.info("ProxyLoadJob end...");
    }
}
