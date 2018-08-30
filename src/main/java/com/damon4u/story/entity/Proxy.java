package com.damon4u.story.entity;

import lombok.Data;
import org.apache.http.HttpHost;

import java.io.Serializable;

/**
 * Description:
 *
 * @author damon4u
 * @version 2018-08-28 10:49
 */
@Data
public class Proxy implements Serializable {

    private String ip;
    
    private int port;
    
    private String type;      //http、https
    
    private boolean availableFlag;
    
    private boolean anonymousFlag;
    
    private long lastSuccessfulTime;//最近一次请求成功时间
    
    private long successfulTotalTime;//请求成功总耗时
    
    private int failureTimes;//请求失败次数
    
    private int successfulTimes;//请求成功次数
    
    private double successfulAverageTime;//成功请求平均耗时

    public Proxy() {}

    public Proxy(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.type = "http";
    }

    public Proxy(String ip, int port, String type) {
        this.ip = ip;
        this.port = port;
        this.type = type.toLowerCase();
    }

    public String getProxyStr() {
        return type + "://" + ip + ":" + port;
    }

    public HttpHost toHttpHost() {
        return new HttpHost(ip,port,type);
    }

}
