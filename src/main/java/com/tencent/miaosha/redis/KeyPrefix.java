package com.tencent.miaosha.redis;

public interface KeyPrefix {

    int expireSeconds();

    String getPrefix();
}
