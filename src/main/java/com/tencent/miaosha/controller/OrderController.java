package com.tencent.miaosha.controller;

import com.tencent.miaosha.domain.MiaoshaUser;
import com.tencent.miaosha.domain.OrderInfo;
import com.tencent.miaosha.redis.RedisService;
import com.tencent.miaosha.service.GoodsService;
import com.tencent.miaosha.service.MiaoshaUserService;
import com.tencent.miaosha.service.OrderService;
import com.tencent.miaosha.utils.CodeMsg;
import com.tencent.miaosha.utils.Result;
import com.tencent.miaosha.vo.GoodsVo;
import com.tencent.miaosha.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {


        @Autowired
        MiaoshaUserService userService;

        @Autowired
        RedisService redisService;

        @Autowired
        OrderService orderService;

        @Autowired
        GoodsService goodsService;

        @RequestMapping("/detail")
        @ResponseBody
        public Result<OrderDetailVo> info(Model model, MiaoshaUser user,
                                          @RequestParam("orderId") long orderId) {
            if(user == null) {
                return Result.error(CodeMsg.SESSION_ERROR);
            }
            OrderInfo order = orderService.getOrderById(orderId);
            if(order == null) {
                return Result.error(CodeMsg.ORDER_NOT_EXIST);
            }
            long goodsId = order.getGoodsId();
            GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
            OrderDetailVo vo = new OrderDetailVo();
            vo.setOrder(order);
            vo.setGoods(goods);
            //System.out.println(vo.toString());
            return Result.success(vo);
        }



}
