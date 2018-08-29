package com.damon4u.story.loader;

import com.damon4u.story.entity.Proxy;
import com.damon4u.story.util.HttpUtil;
import com.damon4u.story.util.UAUtil;
import com.google.common.collect.Lists;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 *
 * @author damon4u
 * @version 2018-08-28 10:57
 */
public class ProxyLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyLoader.class);
    
    public static void loadFromXici() {
        String url = "http://www.xicidaili.com/nn/1.html";
        List<Header> headers = Lists.newArrayList();
        headers.add(new BasicHeader("User-Agent", UAUtil.getUA()));
        String response = HttpUtil.get(url, headers);
        
        List<Proxy> proxyList = parse(response);
        for (Proxy proxy : proxyList) {
            validate(proxy.toHttpHost());
        }
        
    }

    public static List<Proxy> parse(String html) {
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

    public static boolean validate(HttpHost proxy) {
        String validateUrl = "http://www.baidu.com/";
        List<Header> headers = Lists.newArrayList();
        headers.add(new BasicHeader("User-Agent", UAUtil.getUA()));
        headers.add(new BasicHeader("Proxy-Authorization", getAuth()));
        String response = HttpUtil.getWithProxy(validateUrl, proxy, headers, null);
        LOGGER.info("response={}", response);
        return true;
    }
    
    public static String getAuth() {
        return "Basic " + base64("damon4u123@163.com" + ":" + "RFvNzHvvUV67au");
    }

    public static String base64(String data) {
        return Base64.encodeBase64String(data.getBytes(Charset.forName("UTF-8")));
    }

    public static void main(String[] args) {
//        loadFromXici();
        boolean response = validate(new HttpHost("58.209.89.183", 23564, "http"));
        LOGGER.info("response={}", response);
        System.out.println(getAuth());
//        String s = HttpUtil.get("http://www.baidu.com/");
//        LOGGER.info("s={}", s);
    }
}
