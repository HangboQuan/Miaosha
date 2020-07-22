package com.tencent.miaosha.redis;

public class OrderKey extends BasePrefix {

    public OrderKey(String prefix) {
        super(prefix);
    }

    public static OrderKey getKeyByUidGid = new OrderKey("moug");
}
