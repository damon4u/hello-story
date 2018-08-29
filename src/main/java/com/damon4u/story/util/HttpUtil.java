package com.damon4u.story.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class HttpUtil {
    private static Logger log = LoggerFactory.getLogger(HttpUtil.class);

    private static final int MAX_TOTAL_CONNECTIONS = 500;
    private static final int MAX_PER_ROUTE = 100;
    private static final int CONNECTION_TIMEOUT = 5000;
    private static final int SOCKET_TIMEOUT = 10000;
    private static final int CONNECTION_MANAGER_TIMEOUT = 2000;

    private static CloseableHttpClient client;
    private static ExecutorService executorService;

    static {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setCharset(Consts.UTF_8).build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultConnectionConfig(connectionConfig);
        connManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        connManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .setConnectionRequestTimeout(CONNECTION_MANAGER_TIMEOUT)
                .build();
        client = HttpClients.custom().setConnectionManager(connManager).setDefaultRequestConfig(requestConfig).build();
        executorService = new ThreadPoolExecutor(0,2,10, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>(1000),new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static String get(String url) {
        return get(url, null, null);
    }

    public static String get(String url, String charset) {
        return get(url, null, charset);
    }

    public static String get(String url, List<Header> header) {
        return get(url, header, null);
    }

    public static String get(String url, List<Header> header, String charset) {
        HttpGet method = new HttpGet(url);
        try {
            if(CollectionUtils.isNotEmpty(header)){
                for (Header h : header) {
                    method.addHeader(h);
                }
            }
            CloseableHttpResponse response = client.execute(method);
            try {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    return EntityUtils.toString(response.getEntity(), charset);
                }
            } finally {
                response.close();
            }
            log.warn("http get:" + response.getStatusLine() + " url:" + url);
        } catch (IOException e) {
            log.warn("HTTP GET请求发生异常:" + e.getMessage(), e);
        } finally {
            method.releaseConnection();
        }
        return null;
    }

    public static String getWithProxy(String url, HttpHost proxy, List<Header> header, String charset) {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setCharset(Consts.UTF_8).build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultConnectionConfig(connectionConfig);
        connManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        connManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .setConnectionRequestTimeout(CONNECTION_MANAGER_TIMEOUT)
                .setProxy(proxy)
                .build();
        CloseableHttpClient httpClient = HttpClients.custom().
                setConnectionManager(connManager).
                setDefaultRequestConfig(requestConfig).build();
        HttpGet method = new HttpGet(url);
        try {
            if(CollectionUtils.isNotEmpty(header)){
                for (Header h : header) {
                    method.addHeader(h);
                }
            }
            CloseableHttpResponse response = httpClient.execute(method);
            try {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    return EntityUtils.toString(response.getEntity(), charset);
                }
            } finally {
                response.close();
            }
            log.warn("http get:" + response.getStatusLine() + " url:" + url);
        } catch (IOException e) {
            log.warn("HTTP GET请求发生异常:" + e.getMessage(), e);
        } finally {
            method.releaseConnection();
        }
        return null;
    }

    public static String post(String url, NameValuePair... params) {
        return post(url, Arrays.asList(params));
    }

    public static String post(String url, List<NameValuePair> params, String... encoding) {
        return post(url, null, null, params, encoding);
    }

    public static String post(String url, List<Header> header, String body) {
        return post(url, header, body, null);
    }

    public static String post(String url, List<Header> header, List<NameValuePair> params) {
        return post(url, header, null, params);
    }

    public static String post(String url, List<Header> header, String body, List<NameValuePair> params, String... encoding) {
        HttpPost method = new HttpPost(url);
        try {
            if (CollectionUtils.isNotEmpty(params)) {
                if (ArrayUtils.isNotEmpty(encoding) && StringUtils.isNotBlank(encoding[0])) {
                    method.setEntity(new UrlEncodedFormEntity(params, encoding[0]));
                } else {
                    method.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
                }
            }
            if (body != null) {
                if (ArrayUtils.isNotEmpty(encoding) && StringUtils.isNotBlank(encoding[0])) {
                    method.setEntity(new StringEntity(body, encoding[0]));
                } else {
                    method.setEntity(new StringEntity(body, Consts.UTF_8));
                }
            }
            if(CollectionUtils.isNotEmpty(header)){
                for (Header h : header) {
                    method.addHeader(h);
                }
            }
            CloseableHttpResponse response = client.execute(method);
            try {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    return EntityUtils.toString(response.getEntity());
                }
            } finally {
                response.close();
            }
            log.warn("http post:" + response.getStatusLine() + " url:" + url);
        } catch (IOException e) {
            log.warn("HTTP POST请求发生异常. url:" + url + ", params:" + JSONUtil.toJson(params) + ", e.getMessage():" + e.getMessage(), e);
        } finally {
            method.releaseConnection();
        }
        return null;
    }

    public static String delete(String url, NameValuePair... params) {
        url = addRequestParams(url, Arrays.asList(params));
        HttpDelete method = new HttpDelete(url);
        try {
            CloseableHttpResponse response = client.execute(method);
            try {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    return EntityUtils.toString(response.getEntity());
                }
                log.warn("http delete:" + response.getStatusLine());
            } finally {
                response.close();
            }
        } catch (IOException e) {
            log.warn("HTTP DELETE请求发生异常:" + e.getMessage(), e);
        } finally {
            method.releaseConnection();
        }
        return null;
    }

    private static String addRequestParams(String url, List<NameValuePair> params){
        if (!url.endsWith("?")) {
            url += "?";
        }
        String paramString = URLEncodedUtils.format(params, "utf-8");
        url += paramString;
        return url;
    }

    public static Future<String> postAsyn(final String url, final NameValuePair... params){
        return executorService.submit(new Callable<String>(){

            @Override
            public String call() throws Exception {
                return post(url,params);
            }});
    }

}


