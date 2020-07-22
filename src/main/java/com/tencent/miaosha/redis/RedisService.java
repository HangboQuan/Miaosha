package com.tencent.miaosha.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisService {

    @Autowired
    JedisPool jedisPool;


    public <T> T getKey(KeyPrefix prefix, String key, Class<T> clazz){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String real = prefix.getPrefix() + key;
            String str = jedis.get(real);
            T t = stringToBean(str, clazz);
            return t;
        }finally {
            returnToPool(jedis);
        }
    }

    public <T> Boolean setKey(KeyPrefix prefix, String key, T value){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String str = beanToString(value);
            if (str == null || str.length() <= 0) {
                return false;
            }
            String real = prefix.getPrefix() + key;
            int seconds = prefix.expireSeconds();
            if (seconds <=0 ){
                jedis.set(real, str);
            } else{
                jedis.setex(real,seconds,str);
            }

            return true;
        }finally {
            returnToPool(jedis);
        }
    }

    public static <T> String beanToString(T value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();

        if (clazz == int.class || clazz == Integer.class){
            return "" + value;
        } else if(clazz == String.class) {
            return (String)value;
        } else if(clazz == long.class || clazz == Long.class) {
            return "" + value;
        } else{
            return JSON.toJSONString(value);
        }

    }

    @SuppressWarnings("unchecked")
    public static <T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.length() <=0 || clazz == null) {
            return null;
        }
        if (clazz == int.class || clazz == Integer.class){
            return (T)Integer.valueOf(str);
        } else if(clazz == String.class) {
            return (T)str;
        } else if(clazz == long.class || clazz == Long.class) {
            return (T)Long.valueOf(str);
        } else{
            return JSON.toJavaObject(JSON.parseObject(str),clazz);
        }
    }



    private void returnToPool(Jedis jedis) {

        if (jedis != null) {
            jedis.close();
        }
    }

    public <T> Boolean exists(KeyPrefix prefix, String key){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String real = prefix.getPrefix() + key;
            return jedis.exists(real);
        }finally {
            returnToPool(jedis);
        }
    }

    public <T> Long incr(KeyPrefix prefix, String key){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String real = prefix.getPrefix() + key;
            return jedis.incr(real);
        }finally {
            returnToPool(jedis);
        }
    }

    public <T> Long decr(KeyPrefix prefix, String key){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String real = prefix.getPrefix() + key;
            return jedis.decr(real);
        }finally {
            returnToPool(jedis);
        }
    }

    public <T> Long delete(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String real = prefix.getPrefix() + key;
            return jedis.del(real);
        }finally {
            returnToPool(jedis);
        }
    }
}
