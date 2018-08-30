package com.damon4u.story.entity;

import lombok.Data;
import org.apache.http.HttpHost;

/**
 * Description:
 *
 * @author damon4u
 * @version 2018-08-30 18:19
 */
@Data
public class GitProxy {
    
    private String proto;
    
    private String ip;
    
    private int port;

    public String getProxyStr() {
        return proto + "://" + ip + ":" + port;
    }

    public HttpHost toHttpHost() {
        return new HttpHost(ip, port, proto);
    }
}
