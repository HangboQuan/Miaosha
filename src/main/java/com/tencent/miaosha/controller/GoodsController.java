package com.tencent.miaosha.controller;

import com.tencent.miaosha.domain.MiaoshaUser;
import com.tencent.miaosha.redis.GoodsKey;
import com.tencent.miaosha.redis.RedisService;
import com.tencent.miaosha.service.GoodsService;
import com.tencent.miaosha.service.MiaoshaUserService;

import com.tencent.miaosha.utils.Result;
import com.tencent.miaosha.vo.GoodsDetailVo;
import com.tencent.miaosha.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;


import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/goods")
public class GoodsController {

    private static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    MiaoshaUserService miaoshaUserService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    RedisService redisService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;


    @RequestMapping(value = "/to_list", produces = "text/html")
    @ResponseBody
    public String toLogin(HttpServletRequest request,HttpServletResponse response,Model model, MiaoshaUser miaoshaUser) {

        model.addAttribute("user", miaoshaUser);

        //1.取缓存
        String html = redisService.getKey(GoodsKey.getGoodsList,"",String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        List<GoodsVo> goodsList = goodsService.listGoodsVo();

        model.addAttribute("goodsList",goodsList);

        //return "goods_list";

        SpringWebContext ctx = new SpringWebContext(request,response,
                request.getServletContext(),request.getLocale(), model.asMap(), applicationContext);

        html = thymeleafViewResolver.getTemplateEngine().process("goods_list",ctx);
        if(!StringUtils.isEmpty(html)){
            redisService.setKey(GoodsKey.getGoodsList,"",html);
        }
        return html;
    }


    @RequestMapping("/to_detail2/{goodsId}")
    @ResponseBody
    public String goodsDetail2(HttpServletRequest request,HttpServletResponse response,Model model, MiaoshaUser miaoshaUser, @PathVariable("goodsId")long goodsId) {

        model.addAttribute("user", miaoshaUser);

        //1.去缓存
        String html = redisService.getKey(GoodsKey.getGoodsDetail,""+goodsId,String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods",goods);
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();


        int miaoshaStatus = 0;
        int remainSeconds = 0;
        if (now < startAt){
            //秒杀还没开始
            miaoshaStatus = 0;
            remainSeconds = (int)((startAt - now)/1000);

        }else if(now > endAt){
            //秒杀已结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else{
            //正在秒杀
            miaoshaStatus = 1;
            remainSeconds = 0;

        }

        model.addAttribute("miaoshaStatus",miaoshaStatus);
        model.addAttribute("remainSeconds",remainSeconds);


        //return "goods_detail";
        //手动渲染
        SpringWebContext cxt = new SpringWebContext(request,response,request.getServletContext(),request.getLocale(),
                model.asMap(),applicationContext);
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail",cxt);
        if(!StringUtils.isEmpty(html)){
            redisService.setKey(GoodsKey.getGoodsDetail,"",html);
        }
        return html;

    }


    @RequestMapping(value="/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser miaoshaUser, @PathVariable("goodsId")long goodsId){

        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        long startAt = goodsVo.getStartDate().getTime();
        long endAt = goodsVo.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int miaoshaStatus = 0;
        //表示秒杀未开始的倒计时
        int remainSeconds = 0;
        if(now < startAt){
            miaoshaStatus = 0;
            remainSeconds = (int)((startAt - now)/1000);
        }else if(now > endAt){
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else{
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoodsVo(goodsVo);
        vo.setMiaoshaUser(miaoshaUser);
        vo.setRemainSeconds(remainSeconds);
        vo.setMiaoshaStatus(miaoshaStatus);
        //System.out.println(vo.toString());
        return Result.success(vo);
    }



}
