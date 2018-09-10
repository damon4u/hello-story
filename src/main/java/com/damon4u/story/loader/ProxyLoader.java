package com.damon4u.story.loader;

import com.damon4u.story.dao.ProxyDao;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description:
 *
 * @author damon4u
 * @version 2018-08-28 10:57
 */
@Component
public class ProxyLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyLoader.class);

    private static final Pattern PATTERN_IP_PORT = Pattern.compile("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))):\\d{0,5}");
    
    @Resource
    private ProxyDao proxyDao;
    
    public void loadProxy() {
        loadFromGit();
    }
    
    public void loadFromGit() {
        LOGGER.info("loadFromGit start...");
        String url = "https://raw.githubusercontent.com/stamparm/aux/master/fetch-some-list.txt";
        List<Header> headers = Lists.newArrayList();
        headers.add(new BasicHeader("User-Agent", UAUtil.getUA()));
        String response = HttpUtil.get(url, headers);
        if (response == null) {
            return;
        }
        List<Proxy> gitProxyList = JSONUtil.fromJsonList(response, Proxy.class);
        List<HttpHost> proxyList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(gitProxyList)) {
            for (Proxy gitProxy : gitProxyList) {
                LOGGER.info("gitProxy={}", gitProxy.getProxyStr());
                proxyList.add(gitProxy.toHttpHost());
            }
        }
        filterProxy(proxyList);
        LOGGER.info("loadFromGit done.");
    }

    public void loadFromXici() {
        LOGGER.info("loadFromXici start...");
        String url = "http://www.xicidaili.com/nn/1.html";
        List<Header> headers = Lists.newArrayList();
        headers.add(new BasicHeader("User-Agent", UAUtil.getUA()));
        String response = HttpUtil.get(url, headers);

        List<Proxy> xiciProxyList = parseXici(response);
        List<HttpHost> proxyList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(xiciProxyList)) {
            for (Proxy xiciProxy : xiciProxyList) {
                LOGGER.info("xiciProxy={}", xiciProxy.getProxyStr());
                proxyList.add(xiciProxy.toHttpHost());
            }
        }
        filterProxy(proxyList);
        LOGGER.info("loadFromXici done.");
    }

    public void loadFrom66ip() {
        LOGGER.info("loadFrom66ip start...");
        String url = "http://www.66ip.cn/mo.php?sxb=&tqsl=1000&port=&export=&ktip=&sxa=&submit=%CC%E1++%C8%A1&textarea=";
        List<Header> headers = Lists.newArrayList();
        headers.add(new BasicHeader("User-Agent", UAUtil.getUA()));
        String response = HttpUtil.get(url, headers);
        Matcher matcher = PATTERN_IP_PORT.matcher(response);
        List<HttpHost> proxyList = Lists.newArrayList();
        while (matcher.find()) {
            String group = matcher.group();
            String[] split = group.split(":");
            String host = split[0];
            String ip = split[1];
            proxyList.add(new HttpHost(host, Integer.valueOf(ip)));
            LOGGER.info(group);
        }

        filterProxy(proxyList);
        LOGGER.info("loadFrom66ip done.");
    }
    
    private List<Proxy> parseXici(String html) {
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
    
    public void filterProxy(List<HttpHost> proxyList) {
        if (CollectionUtils.isNotEmpty(proxyList)) {
            final CountDownLatch latch = new CountDownLatch(proxyList.size());
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            for (HttpHost originProxy : proxyList) {
                executorService.execute(new FilterJob(originProxy, latch));
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
            executorService.shutdownNow();
        }
    }

    class FilterJob implements Runnable {
        
        private HttpHost proxy;
        
        private CountDownLatch latch;

        public FilterJob(HttpHost proxy, CountDownLatch latch) {
            this.proxy = proxy;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                // 先只要http类型的
                if (proxy.getSchemeName().startsWith("http") && validateProxy(proxy)) {
                    LOGGER.info("================> {}", proxy);
                    proxyDao.save(new Proxy(proxy.getHostName(), proxy.getPort(), proxy.getSchemeName()));
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            } finally {
                latch.countDown();
            }
        }
    }

    public boolean validateProxy(HttpHost proxy) {
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

    public void validateFromDb() {
        List<Proxy> allProxy = proxyDao.getAllProxy();
        if (CollectionUtils.isNotEmpty(allProxy)) {
            final CountDownLatch latch = new CountDownLatch(allProxy.size());
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            for (Proxy proxy : allProxy) {
                executorService.execute(new ValidateJob(proxy, latch));
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
            executorService.shutdownNow();
        }
    }

    class ValidateJob implements Runnable {

        private Proxy proxy;

        private CountDownLatch latch;

        public ValidateJob(Proxy proxy, CountDownLatch latch) {
            this.proxy = proxy;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                if (!validateProxy(proxy.toHttpHost())) {
                    proxyDao.delete(proxy.getId());
                    LOGGER.error("disable proxy={}", proxy.getProxyStr());
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            } finally {
                latch.countDown();
            }
        }
    }
    
}
