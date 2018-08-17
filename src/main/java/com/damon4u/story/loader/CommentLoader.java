package com.damon4u.story.loader;

import com.alibaba.fastjson.JSONObject;
import com.damon4u.story.dao.CommentDao;
import com.damon4u.story.dao.SongDao;
import com.damon4u.story.dao.UserDao;
import com.damon4u.story.entity.Comment;
import com.damon4u.story.entity.CommentReplied;
import com.damon4u.story.entity.CommentResponse;
import com.damon4u.story.entity.Song;
import com.damon4u.story.entity.User;
import com.damon4u.story.util.HttpUtil;
import com.damon4u.story.util.UAUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
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

//    private static final String KEY_SONG_INFO = "song_info";
//    private static final String KEY_SONG_COMMENT = "song_comment";
//    private static final String KEY_COMMENT_RANK = "comment_rank";

    private static Pattern NAME_PATTERN = Pattern.compile("<title>(.*) - 网易云音乐</title>");
    private static Pattern DESCRIPTION_PATTERN = Pattern.compile("<meta name=\"description\" content=\"(.*)\" />");
    private static Pattern IMAGE_PATTERN = Pattern.compile("<meta property=\"og:image\" content=\"(.*)\" />");

    @Resource
    private SongDao songDao;
    
    @Resource
    private UserDao userDao;
    
    @Resource
    private CommentDao commentDao;
    
//    @Resource
//    private JedisTemplate jedisTemplate;

    /**
     * 获取歌曲和评论信息
     *
     * @param start 歌曲id起始
     * @param end 歌曲id结束
     */
    public void loadSongs(int start, int end) {
        for(int songId = start; songId <= end; songId++) {
            Song songInfo = getSongInfo(songId);
            if (songInfo != null) {
                songDao.save(songInfo);
                loadCommentInfo(songId);
            }
        }
    }

    /**
     * 获取歌曲评论
     * @param songId 歌曲id
     */
    private void loadCommentInfo(long songId) {
        String url = "http://music.163.com/weapi/v1/resource/comments/R_SO_4_" + songId + "/?csrf_token=d2c9e86c94efabcc4b5a1a6d757d417e";
        List<Header> headers = Lists.newArrayList();
        headers.add(new BasicHeader("User-Agent", UAUtil.getUA()));
        headers.add(new BasicHeader("Referer", "http://music.163.com/"));
        headers.add(new BasicHeader("Connection", "keep-alive/"));
        List<NameValuePair> params = Lists.newArrayList();
        params.add(new BasicNameValuePair("params", "flQdEgSsTmFkRagRN2ceHMwk6lYVIMro5auxLK/JywlqdjeNvEtiWDhReFI+QymePGPLvPnIuVi3dfsDuqEJW204VdwvX+gr3uiRBeSFuOm1VUSJ1HqOc+nJCh0j6WGUbWuJC5GaHTEE4gcpWXX36P4Eu4djoQBzoqdsMbCwoolb2/WrYw/N2hehuwBHO4Oz"));
        params.add(new BasicNameValuePair("encSecKey", "0263b1cd3b0a9b621a819b73e588e1cc5709349b21164dc45ab760e79858bb712986ea064dbfc41669e527b767f02da7511ac862cbc54ea7d164fc65e0359962273616e68e694453fb6820fa36dd9915b2b0f60dadb0a6022b2187b9ee011b35d82a1c0ed8ba0dceb877299eca944e80b1e74139f0191adf71ca536af7d7ec25"));
        String response = HttpUtil.post(url,headers, null, params, "utf-8");
        if (StringUtils.isNotBlank(response)) {
            JSONObject res = JSONObject.parseObject(response);
            long commentCount = res.getLongValue("total"); // 歌曲总评论数
            if (commentCount < 1000) {
                // 总评论数少于1000的歌曲就扔掉了
                return;
            }
//            String songIdStr = String.valueOf(songId);
//            jedisTemplate.zadd(KEY_COMMENT_RANK, commentCount, songIdStr);
            CommentResponse[] hotCommentResponses = res.getObject("hotComments", CommentResponse[].class);
            if (hotCommentResponses != null && hotCommentResponses.length > 0) {
                for (CommentResponse commentResponse : hotCommentResponses) {
//                    jedisTemplate.zadd(KEY_SONG_COMMENT + "#" + songIdStr, commentResponse.getLikedCount(), JSONUtil.toJson(commentResponse));
                    Long userId = 0L;
                    User user = commentResponse.getUser();
                    if (user != null) {
                        userId = user.getUserId();
                        userDao.save(user);
                    }
                    Long beRepliedUserId = 0L;
                    String beRepliedContent = "";
                    List<CommentReplied> beRepliedList = commentResponse.getBeReplied();
                    if (CollectionUtils.isNotEmpty(beRepliedList)) {
                        CommentReplied commentReplied = beRepliedList.get(0);
                        User repliedUser = commentReplied.getUser();
                        if (repliedUser != null) {
                            beRepliedUserId = repliedUser.getUserId();
                            userDao.save(repliedUser);
                        }
                        beRepliedContent = commentReplied.getContent();
                    }
                    
                    Comment comment = new Comment();
                    comment.setCommentId(commentResponse.getCommentId());
                    comment.setSongId(songId);
                    comment.setLikedCount(commentResponse.getLikedCount());
                    comment.setContent(commentResponse.getContent());
                    comment.setUserId(userId);
                    comment.setBeRepliedUserId(beRepliedUserId);
                    comment.setBeRepliedContent(beRepliedContent);
                    comment.setCommentTime(new Date(commentResponse.getTime()));
                    comment.setCreateTime(new Date());
                    commentDao.save(comment);
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
    public Song getSongInfo(long songId) {
        String url = "http://music.163.com/song?id=" + songId;
        List<Header> headerList = Lists.newArrayList();
        headerList.add(new BasicHeader("User-Agent", UAUtil.getUA()));
        headerList.add(new BasicHeader("Referer", "http://music.163.com/"));
        headerList.add(new BasicHeader("Connection", "keep-alive/"));
        String response = HttpUtil.get(url, headerList, "utf-8");
        if (StringUtils.isBlank(response)) {
            return null;
        }
        Matcher nameMatcher = NAME_PATTERN.matcher(response);
        Matcher descriptionMatcher = DESCRIPTION_PATTERN.matcher(response);
        Matcher imageMatcher = IMAGE_PATTERN.matcher(response);
        String name = "";
        String description = "";
        String image = "";
        if (nameMatcher.find()) {
            name = nameMatcher.group(1);
//            jedisTemplate.hset(KEY_SONG_INFO, String.valueOf(songId), name);
        }
        if (descriptionMatcher.find()) {
            description = descriptionMatcher.group(1);
        }
        if (imageMatcher.find()) {
            image = imageMatcher.group(1);
        }
        if (StringUtils.isNotBlank(name)) {
            Song song = new Song();
            song.setSongId(songId);
            song.setName(name);
            song.setDescription(description);
            song.setImage(image);
            song.setCreateTime(new Date());
            logger.info("song={}", song);
            return song;
        }
        return null;
    }

}
