package com.hscloud.hs.cost.account.utils;

import com.alibaba.fastjson.JSON;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;



    // String operations
    public void set(String key, Object value) {
        set(key, value, CacheConstants.duration + new Random().nextInt(1000), TimeUnit.SECONDS);
    }

    public void set(String key, Object value, long expiration, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, expiration, timeUnit);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // List operations
    public void lPush(String key, Object value) {
        lPush(key, value, CacheConstants.duration + new Random().nextInt(1000), TimeUnit.SECONDS);
    }

    public void lPush(String key, Object value, long expiration, TimeUnit timeUnit) {
        redisTemplate.opsForList().leftPush(key, value);
        redisTemplate.expire(key, expiration, timeUnit);
    }

    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    // Set operations
    public void sAdd(String key, Object... values) {
        sAdd(key, CacheConstants.duration + new Random().nextInt(1000), TimeUnit.SECONDS, values);
    }

    public void sAdd(String key, long expiration, TimeUnit timeUnit, Object... values) {
        redisTemplate.opsForSet().add(key, values);
        redisTemplate.expire(key, expiration, timeUnit);
    }

    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    // Hash operations
    public void hSet(String key, String field, Object value) {
        hSet(key, field, value, CacheConstants.duration + new Random().nextInt(1000), TimeUnit.SECONDS);
    }

    public void hSet(String key, String field, Object value, long expiration, TimeUnit timeUnit) {
        redisTemplate.opsForHash().put(key, field, value);
        redisTemplate.expire(key, expiration, timeUnit);
    }

    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    public void putObject(String key, Map<String,Object> map,long expiration, TimeUnit timeUnit) {
        redisTemplate.opsForHash().putAll(key, map);
        redisTemplate.expire(key, expiration, timeUnit);
    }

    public <T> T getObject(String key, Class<T> clazz) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        // 这里需要将 Map 转换成对应的对象
        return JSON.parseObject(JSON.toJSONString(entries), clazz);
    }

    // Sorted Set operations
    public void zAdd(String key, Object value, double score) {
        zAdd(key, value, score, CacheConstants.duration + new Random().nextInt(1000), TimeUnit.SECONDS);
    }

    public void zAdd(String key, Object value, double score, long expiration, TimeUnit timeUnit) {
        redisTemplate.opsForZSet().add(key, value, score);
        redisTemplate.expire(key, expiration, timeUnit);
    }

    public Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    // Transaction Pipeline
    public void executeTransactionPipeline(List<String> keys) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String key : keys) {
                connection.incr(key.getBytes());
            }
            return null;
        });
    }

    public List<Object> executeTransaction(List<String> keys) {
        return redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConn = (StringRedisConnection)connection;
            for (String key : keys) {
                stringRedisConn.incr(key);
            }
            return null;
        });
    }


    public void expire(String key, Long duration) {
        redisTemplate.expire(key, duration, TimeUnit.SECONDS);
    }


    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     * @param timeout 时间
     * @param timeUnit 时间颗粒度
     */
    public <T> Boolean setLock(final String key, final T value, final Long timeout, final TimeUnit timeUnit)
    {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit);
    }

    /**
     * 删除锁
     *
     * @param key
     */
    public Boolean unLock(final String key)
    {
        return redisTemplate.delete(key);
    }

    public void unLock(String key, String value) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "return redis.call('del', KEYS[1]) " +
                "else return 0 end";
        redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Collections.singletonList(key), value);
    }
}
