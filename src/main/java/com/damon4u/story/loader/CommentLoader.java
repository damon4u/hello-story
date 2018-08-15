package com.damon4u.story.loader;

import com.alibaba.fastjson.JSONObject;
import com.damon4u.story.cache.JedisTemplate;
import com.damon4u.story.entity.Comment;
import com.damon4u.story.util.HttpUtil;
import com.damon4u.story.util.JSONUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description:
 *
 * @author damon4u
 * @version 2017-05-21 14:19
 */
@Component
public class CommentLoader {

    private static final Logger logger = LoggerFactory.getLogger(CommentLoader.class);

    private static final String KEY_SONG_INFO = "song_info";
    private static final String KEY_SONG_COMMENT = "song_comment";
    private static final String KEY_COMMENT_RANK = "comment_rank";

    private static Pattern SONG_PATTERN = Pattern.compile("<title>(.*) - 网易云音乐</title>");

    @Resource
    private JedisTemplate jedisTemplate;

    /**
     * 获取歌曲和评论信息
     *
     * @param start 歌曲id起始
     * @param end 歌曲id结束
     */
    public void loadSongs(int start, int end) throws IOException {
        for(int songId = start; songId <= end; songId++) {
            String songInfo = getSongInfo(songId);
            if (StringUtils.isNotBlank(songInfo)) {
                loadCommentInfo(songId);
            }
        }
    }

    /**
     * 获取歌曲评论
     * @param songId 歌曲id
     */
    private void loadCommentInfo(long songId) throws IOException {
        String url = "http://music.163.com/weapi/v1/resource/comments/R_SO_4_" + songId + "/?csrf_token=d2c9e86c94efabcc4b5a1a6d757d417e";
        List<Header> headers = Lists.newArrayList();
        headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36"));
        List<NameValuePair> params = Lists.newArrayList();
        params.add(new BasicNameValuePair("params", "flQdEgSsTmFkRagRN2ceHMwk6lYVIMro5auxLK/JywlqdjeNvEtiWDhReFI+QymePGPLvPnIuVi3dfsDuqEJW204VdwvX+gr3uiRBeSFuOm1VUSJ1HqOc+nJCh0j6WGUbWuJC5GaHTEE4gcpWXX36P4Eu4djoQBzoqdsMbCwoolb2/WrYw/N2hehuwBHO4Oz"));
        params.add(new BasicNameValuePair("encSecKey", "0263b1cd3b0a9b621a819b73e588e1cc5709349b21164dc45ab760e79858bb712986ea064dbfc41669e527b767f02da7511ac862cbc54ea7d164fc65e0359962273616e68e694453fb6820fa36dd9915b2b0f60dadb0a6022b2187b9ee011b35d82a1c0ed8ba0dceb877299eca944e80b1e74139f0191adf71ca536af7d7ec25"));
        String response = HttpUtil.post(url,headers, params);
        if (StringUtils.isNotBlank(response)) {
            JSONObject res = JSONObject.parseObject(response);
            long commentCount = res.getLongValue("total"); // 歌曲总评论数
            if (commentCount < 1000) {
                // 总评论数少于1000的歌曲就扔掉了
                return;
            }
            String songIdStr = String.valueOf(songId);
            jedisTemplate.zadd(KEY_COMMENT_RANK, commentCount, songIdStr);
            Comment[] hotComments = res.getObject("hotComments", Comment[].class);
            if (hotComments != null && hotComments.length > 0) {
                for (Comment comment : hotComments) {
                    jedisTemplate.zadd(KEY_SONG_COMMENT + "#" + songIdStr, comment.getLikedCount(), JSONUtil.toJson(comment));
                }
            }
        }
    }

    /**
     * 获取歌曲信息
     *
     * @param songId 歌曲id
     * @return 如果找到，返回：歌曲名称 - 歌手名称；否则返回null
     */
    public String getSongInfo(long songId) {
        String url = "http://music.163.com/song?id=" + songId;
        List<Header> headerList = Lists.newArrayList();
        headerList.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36"));
        String response = HttpUtil.get(url, headerList);
        Matcher matcher = SONG_PATTERN.matcher(response);
        if (matcher.find()) {
            String songInfo = matcher.group(1);
            logger.info("getSongInfo success. {}", songInfo);
            jedisTemplate.hset(KEY_SONG_INFO, String.valueOf(songId), songInfo);
            return songInfo;
        }
        return null;
    }

}
