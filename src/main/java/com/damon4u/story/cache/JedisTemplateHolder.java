package com.damon4u.story.cache;

/**
 * 在Spring自动注入之后保存jedisTemplate的指针，以便非SpringBean的类使用
 *
 */
public class JedisTemplateHolder {

    private static JedisTemplate jedisTemplate;

    public static JedisTemplate getJedisTemplate() {
        return jedisTemplate;
    }

    public void setJedisTemplate(JedisTemplate jedisTemplate) {
        JedisTemplateHolder.jedisTemplate = jedisTemplate;
    }
}
