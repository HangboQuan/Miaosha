package com.tencent.miaosha.service;

import com.tencent.miaosha.domain.MiaoshaOrder;
import com.tencent.miaosha.domain.MiaoshaUser;
import com.tencent.miaosha.domain.OrderInfo;
import com.tencent.miaosha.redis.MiaoshaKey;
import com.tencent.miaosha.redis.RedisService;
import com.tencent.miaosha.utils.MD5Utils;
import com.tencent.miaosha.utils.UUIDUtil;
import com.tencent.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class MiaoshaService {


    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    @Transactional
    public OrderInfo miaosha(MiaoshaUser miaoshaUser, GoodsVo goods) {

        goodsService.reduceStock(goods);
        return orderService.createOrder(miaoshaUser, goods);




    }

    /**
     *返回结果：
     * orderId：成功
     * -1：秒杀失败
     * 0：进行中
     */
    public long getMiaoshaResult(Long id, long goodsId) {
        MiaoshaOrder order = orderService.getMiaoshaUserIdGoodsId(id, goodsId);
        if(order != null){
            //秒杀成功
            return order.getOrderId();
        }else{
            boolean isOver = getGoodsOver(goodsId);
            if(isOver){
                return -1;
            }else{
                return 0;
            }
        }

    }

    private void setGoodsOver(long goodsId) {
        redisService.setKey(MiaoshaKey.isGoodsOver,""+goodsId,true);
    }
    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(MiaoshaKey.isGoodsOver,""+goodsId);
    }

    //验证数字验证码
    public boolean checkVerifyCode(MiaoshaUser miaoshaUser, long goodsId, int verifyCode) {
        if(miaoshaUser == null || goodsId <= 0){
            return false;
        }
        Integer codeOId = redisService.getKey(MiaoshaKey.getMiaoshaVerifyCode,miaoshaUser.getId()+","+goodsId,Integer.class);
        if(codeOId == null || codeOId - verifyCode != 0){
            /*System.out.println(codeOId);
            System.out.println(verifyCode);*/
            return false;
        }

        //记得及时删除redis中的验证码,防止对下一次数据造成影响
        redisService.delete(MiaoshaKey.getMiaoshaVerifyCode,miaoshaUser.getId()+","+goodsId);
        return true;

    }

    public String createMiaoshaPath(MiaoshaUser miaoshaUser, long goodsId) {
        if(miaoshaUser == null || goodsId < 0){
            return null;
        }
        String str = MD5Utils.md5(UUIDUtil.uuid() + "123456");
        redisService.setKey(MiaoshaKey.getMiaoshaPath, ""+miaoshaUser.getId()+"_"+goodsId,str);
        return str;
    }

    public BufferedImage createVerfiyCode(MiaoshaUser miaoshaUser, long goodsId) {
        if(miaoshaUser == null || goodsId <=0) {
            return null;
        }
        int width = 100;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        redisService.setKey(MiaoshaKey.getMiaoshaVerifyCode, miaoshaUser.getId()+","+goodsId, rnd);
        //输出图片	
        return image;
    }

    private static int calc(String verifyCode) {
        try{
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer)engine.eval(verifyCode);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }

    }

    private char[] opt = new char[]{'+','-','*'};
    //生成一个验证码
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        int num4 = rdm.nextInt(10);
        String str = "" + num1 + opt[rdm.nextInt(3)]+ num2 + opt[rdm.nextInt(3)]+ num3 + opt[rdm.nextInt(3)] + num4 ;
        return str;
    }

    /*public static void main(String[] args) {
        System.out.println(calc("1+2+3+4"));

    }*/
}
