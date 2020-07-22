package com.tencent.miaosha.rabbitmq;

import com.tencent.miaosha.domain.MiaoshaOrder;
import com.tencent.miaosha.domain.MiaoshaUser;
import com.tencent.miaosha.redis.RedisService;
import com.tencent.miaosha.service.GoodsService;
import com.tencent.miaosha.service.MiaoshaService;
import com.tencent.miaosha.service.OrderService;
import com.tencent.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receive(String message){
        log.info("receive info:"+message);
        MiaoshaMessage mm = RedisService.stringToBean(message,MiaoshaMessage.class);
        MiaoshaUser miaoshaUser = mm.getMiaoshaUser();
        long goodsId = mm.getGoodsId();
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if(stock < 0){
            return;
        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaUserIdGoodsId(miaoshaUser.getId(),goodsId);
        if(order != null){
            return ;
        }
        //减库存 下订单 写入秒杀订单
        miaoshaService.miaosha(miaoshaUser, goods);

    }

}
