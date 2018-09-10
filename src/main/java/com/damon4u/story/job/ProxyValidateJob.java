package com.damon4u.story.job;

import com.damon4u.story.loader.ProxyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * Description:
 * 
 * 定时验证数据库代理池中的代理记录，删除不可用的
 *
 * @author damon4u
 * @version 2018-09-10 11:57
 */
public class ProxyValidateJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyValidateJob.class);
    
    @Resource
    private ProxyLoader proxyLoader;
    
    public void execute() {
        LOGGER.info("ProxyValidateJob start.");
        proxyLoader.validateFromDb();
        LOGGER.info("ProxyValidateJob end.");
    }
}
