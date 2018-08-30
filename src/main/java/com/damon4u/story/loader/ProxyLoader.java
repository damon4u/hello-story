package com.damon4u.story.loader;

import com.damon4u.story.dao.ProxyDao;
import com.damon4u.story.entity.GitProxy;
import com.damon4u.story.entity.Proxy;
import com.damon4u.story.util.HttpUtil;
import com.damon4u.story.util.JSONUtil;
import com.damon4u.story.util.UAUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 *
 * @author damon4u
 * @version 2018-08-28 10:57
 */
@Component
public class ProxyLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyLoader.class);
    
    @Resource
    private ProxyDao proxyDao;
    
    public void loadFromXici() {
        String url = "http://www.xicidaili.com/nn/1.html";
        List<Header> headers = Lists.newArrayList();
        headers.add(new BasicHeader("User-Agent", UAUtil.getUA()));
        String response = HttpUtil.get(url, headers);
        
        List<Proxy> proxyList = parse(response);
        for (Proxy proxy : proxyList) {
            if (validate(proxy.toHttpHost())) {
                LOGGER.info("================> {}", proxy.getProxyStr());
            }
        }
        
    }
    
    public void loadFromGit() {
        String url = "https://raw.githubusercontent.com/stamparm/aux/master/fetch-some-list.txt";
        List<Header> headers = Lists.newArrayList();
        headers.add(new BasicHeader("User-Agent", UAUtil.getUA()));
        String response = HttpUtil.get(url, headers);
        if (response == null) {
            return;
        }
        List<GitProxy> gitProxyList = JSONUtil.fromJsonList(response, GitProxy.class);
        if (CollectionUtils.isNotEmpty(gitProxyList)) {
            for (GitProxy gitProxy : gitProxyList) {
                LOGGER.info("gitProxy={}", gitProxy.getProxyStr());
                // 先只要http类型的
                if (gitProxy.getProto().startsWith("http") && validate(gitProxy.toHttpHost())) {
                    LOGGER.info("================> {}", gitProxy.getProxyStr());
                    proxyDao.save(new Proxy(gitProxy.getIp(), gitProxy.getPort(), gitProxy.getProto()));
                }
            }
        }
    }

    public List<Proxy> parse(String html) {
        Document document = Jsoup.parse(html);
        Elements elements = document.select("table[id=ip_list] tr[class]");
        List<Proxy> proxyList = new ArrayList<>(elements.size());
        for (Element element : elements){
            String ip = element.select("td:eq(1)").first().text();
            String port  = element.select("td:eq(2)").first().text();
            String isAnonymous = element.select("td:eq(4)").first().text();
            String type = element.select("td:eq(5)").first().text();
            LOGGER.info("parse result = "+type+"://"+ip+":"+port+"  "+isAnonymous);
            if(isAnonymous.contains("匿")){
                proxyList.add(new Proxy(ip, Integer.valueOf(port), type));
            }
        }
        return proxyList;
    }

    public boolean validate(HttpHost proxy) {
        String schemeName = proxy.getSchemeName();
        String validateUrl = "http://www.baidu.com/";
        if (schemeName.equalsIgnoreCase("https")) {
            validateUrl = "https://www.baidu.com/";
        }
        List<Header> headers = Lists.newArrayList();
        headers.add(new BasicHeader("User-Agent", UAUtil.getUA()));
        String response = HttpUtil.getWithProxy(validateUrl, proxy, headers);
//        LOGGER.info("response={}", response);
        return response != null;
    }
    
}
