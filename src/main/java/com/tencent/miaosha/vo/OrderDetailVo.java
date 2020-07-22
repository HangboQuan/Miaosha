package com.tencent.miaosha.vo;

import com.tencent.miaosha.domain.OrderInfo;

public class OrderDetailVo {

    private GoodsVo goods;
    private OrderInfo order;

    public GoodsVo getGoods() {
        return goods;
    }

    public void setGoods(GoodsVo goods) {
        this.goods = goods;
    }

    public OrderInfo getOrder() {
        return order;
    }

    public void setOrder(OrderInfo order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "OrderDetailVo{" +
                "goods=" + goods +
                ", order=" + order +
                '}';
    }
}
