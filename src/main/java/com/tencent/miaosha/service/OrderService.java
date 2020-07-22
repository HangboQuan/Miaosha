package com.tencent.miaosha.service;

import com.tencent.miaosha.dao.OrderDao;
import com.tencent.miaosha.domain.MiaoshaOrder;
import com.tencent.miaosha.domain.MiaoshaUser;
import com.tencent.miaosha.domain.OrderInfo;
import com.tencent.miaosha.redis.OrderKey;
import com.tencent.miaosha.redis.RedisService;
import com.tencent.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderService {


    @Autowired(required = false)
    OrderDao orderDao;

    @Autowired(required = false)
    RedisService redisService;

    public MiaoshaOrder getMiaoshaUserIdGoodsId(long id, long goodsId) {

        //return orderDao.getMiaoshaUserIdGoodsId(id, goodsId);
        return redisService.getKey(OrderKey.getKeyByUidGid,""+id+"_"+goodsId,MiaoshaOrder.class);

    }

    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }

    @Transactional
    public OrderInfo createOrder(MiaoshaUser miaoshaUser, GoodsVo goods) {

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getGoodsPrice());
        orderInfo.setOrderChannel(1);
        //0表示新建未支付，1表示已支付，2表示已发货，3.表示以收货，4表示已退货，5表示完成
        orderInfo.setStatus(0);
        orderInfo.setUserId(miaoshaUser.getId());

        orderDao.insert(orderInfo);
        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        miaoshaOrder.setGoodsId(goods.getId());
        miaoshaOrder.setOrderId(orderInfo.getId());
        miaoshaOrder.setUserId(miaoshaUser.getId());
        orderDao.insertMiaoshaOrder(miaoshaOrder);
        redisService.setKey(OrderKey.getKeyByUidGid,""+miaoshaUser.getId()+"_"+goods.getId(),miaoshaOrder);
        return orderInfo;

    }
}
