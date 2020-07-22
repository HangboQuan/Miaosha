package com.tencent.miaosha.controller;

import com.tencent.miaosha.access.AccessLimit;
import com.tencent.miaosha.domain.MiaoshaOrder;
import com.tencent.miaosha.domain.MiaoshaUser;
import com.tencent.miaosha.domain.OrderInfo;
import com.tencent.miaosha.rabbitmq.MQSender;
import com.tencent.miaosha.rabbitmq.MiaoshaMessage;
import com.tencent.miaosha.redis.GoodsKey;
import com.tencent.miaosha.redis.RedisService;
import com.tencent.miaosha.service.GoodsService;
import com.tencent.miaosha.service.MiaoshaService;
import com.tencent.miaosha.service.OrderService;
import com.tencent.miaosha.utils.CodeMsg;
import com.tencent.miaosha.utils.Result;
import com.tencent.miaosha.vo.GoodsVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender mqSender;

    private HashMap<Long, Boolean> localOverMap = new HashMap<Long,Boolean>();
    @RequestMapping("/{path}/do_miaosha1")
    @ResponseBody
    public Result<OrderInfo> list1(Model model, MiaoshaUser miaoshaUser,
                                   @Param("goodsId") long goodsId,
                                   @PathVariable("path") String path) {

        if (miaoshaUser == null) {
            //return "login";
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //1.取库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock < 0) {
            /*model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
            return "miaosha_fail";*/
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //判断是否秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaUserIdGoodsId(miaoshaUser.getId(), goodsId);
        if (order != null) {
            /*model.addAttribute("errmsg",CodeMsg.REPEAT_MIAO_SHA.getMsg());
            return "miaosha_fail";*/
            return Result.error(CodeMsg.REPEAT_MIAO_SHA);
        }

        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = miaoshaService.miaosha(miaoshaUser, goods);
        /*model.addAttribute("orderInfo",orderInfo);
        model.addAttribute("goods",goods);*/

        return Result.success(orderInfo);

    }

    /**
     * QPS:1312
     * 5000 * 10
     * QPS: 2154
     * */
    @RequestMapping(value = "/{path}/do_miaosha",method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model, MiaoshaUser miaoshaUser,
                                   @RequestParam("goodsId") long goodsId,
                                   @PathVariable("path")String path) {
        /**
         * 秒杀接口优化步骤：
         * 1.系统做初始化,把商品库存加载到redis中,
         * 2.收到请求,redis预减库存,库存不够则返回失败,否则执行第三步
         * 3.请求入队,立即返回排队中,
         * 4.请求出队,减库存,生成订单
         * 5.客户端轮询时候秒杀成功(4,5步是并发操作)
         */

        model.addAttribute("user",miaoshaUser);
        if (miaoshaUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //这里的localOverMap采用了内存标记优化,可用来减少redis的访问次数
        boolean isOver = localOverMap.get(goodsId);
        if(isOver){
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //预减库存

        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, ""+ goodsId);
        if(stock < 0){
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaUserIdGoodsId(miaoshaUser.getId(),goodsId);
        if(order != null){
            return Result.error(CodeMsg.REPEAT_MIAO_SHA);
        }
        //入队
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setMiaoshaUser(miaoshaUser);
        mm.setGoodsId(goodsId);
        mqSender.sendMiaoshaMessage(mm);
        return Result.success(0);




    }

    //系统初始化，将库存保存到redis中
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if(goodsList == null){
            return ;
        }
        for(GoodsVo goods : goodsList){
            redisService.setKey(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(),goods.getStockCount());
            localOverMap.put(goods.getId(),false);
        }
    }

    @RequestMapping(value="/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model, MiaoshaUser miaoshaUser, @RequestParam("goodsId") long goodsId){
        model.addAttribute("user",miaoshaUser);
        if(miaoshaUser == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = miaoshaService.getMiaoshaResult(miaoshaUser.getId(),goodsId);
        return Result.success(result);
    }

    @AccessLimit(seconds=5,maxCount=5,needLogin=true)
    @RequestMapping(value = "/path",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest request,MiaoshaUser miaoshaUser,
                                         @RequestParam("goodsId")long goodsId,
                                         @RequestParam(value = "verifyCode", defaultValue = "0")int verifyCode){
        if(miaoshaUser == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        boolean check = miaoshaService.checkVerifyCode(miaoshaUser,goodsId,verifyCode);
        if(!check){

            //System.out.println(check);
            return Result.error(CodeMsg.REQUEST_ILLEGAL);

        }
        String path = miaoshaService.createMiaoshaPath(miaoshaUser,goodsId);
        return Result.success(path);
    }

    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCode(HttpServletResponse response,MiaoshaUser miaoshaUser,
                                               @RequestParam("goodsId")long goodsId){
        if(miaoshaUser == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        try{
            BufferedImage image = miaoshaService.createVerfiyCode(miaoshaUser, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image,"JPEG",out);
            out.flush();
            out.close();
            return null;

        }catch(Exception e){
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }
}
