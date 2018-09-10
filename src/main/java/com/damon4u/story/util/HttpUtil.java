package com.damon4u.story.util;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.*;

public class HttpUtil {
    private static Logger log = LoggerFactory.getLogger(HttpUtil.class);

    private static final int CONNECTION_TIMEOUT_SECONDS = 3;
    private static final int READ_TIMEOUT_SECONDS = 3;

    public static String get(String url, List<Header> header) {
        return getWithProxy(url, null, header);
    }
    
    public static String getWithProxy(String url, HttpHost proxy, List<Header> header) {
        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            addProxy(proxy, builder);
            OkHttpClient okHttpClient = builder.build();
            Request.Builder requestBuilder = new Request.Builder();
            requestBuilder.url(url);
            if (CollectionUtils.isNotEmpty(header)) {
                for (Header h : header) {
                    requestBuilder.addHeader(h.getName(), h.getValue());
                }
            }
            Request request = requestBuilder.build();
            Response response = okHttpClient.newCall(request).execute();
            if (response != null && response.code() == 200) {
                ResponseBody body = response.body();
                if (body != null) {
                    return body.string();
                }
            }
            if (response != null) {
                response.close();
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return null;
    }

    public static String post(String url, List<Header> header, List<NameValuePair> params) {
        return postWithProxy(url, null, header, params);
    }

    public static String postWithProxy(String url, HttpHost proxy, List<Header> header, List<NameValuePair> params) {
        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            addProxy(proxy, builder);
            OkHttpClient okHttpClient = builder.build();

            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            if (CollectionUtils.isNotEmpty(params)) {
                for (NameValuePair nameValuePair : params) {
                    formBodyBuilder.add(nameValuePair.getName(), nameValuePair.getValue());
                }
            }
            FormBody formBody = formBodyBuilder.build();

            Request.Builder requestBuilder = new Request.Builder();
            requestBuilder.url(url).post(formBody);
            if (CollectionUtils.isNotEmpty(header)) {
                for (Header h : header) {
                    requestBuilder.addHeader(h.getName(), h.getValue());
                }
            }
            Request request = requestBuilder.build();
            Response response = okHttpClient.newCall(request).execute();
            if (response != null && response.code() == 200) {
                ResponseBody body = response.body();
                if (body != null) {
                    return body.string();
                }
            }
            if (response != null) {
                response.close();
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return null;
    }

    /**
     * 设置代理
     */
    private static void addProxy(HttpHost proxy, OkHttpClient.Builder builder) {
        if (proxy != null) {
            String schemeName = proxy.getSchemeName();
            Proxy.Type type = Proxy.Type.HTTP;
            if (schemeName.startsWith("socks")) {
                type = Proxy.Type.SOCKS;
            }
            builder.proxy(new Proxy(type, new InetSocketAddress(proxy.getHostName(), proxy.getPort())));
        }
    }

}


