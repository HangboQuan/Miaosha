package com.tencent.miaosha.controller;

import com.tencent.miaosha.domain.User;
import com.tencent.miaosha.rabbitmq.MQSender;
import com.tencent.miaosha.redis.RedisService;
import com.tencent.miaosha.redis.UserKey;
import com.tencent.miaosha.service.UserService;
import com.tencent.miaosha.utils.CodeMsg;
import com.tencent.miaosha.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Hello {


    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender mqSender;

    @RequestMapping("/hello")
    @ResponseBody
    public Result<String> hello() {
        return Result.success("hello");
    }

    @RequestMapping("/helloError")
    @ResponseBody
    public Result<String> error() {
        return Result.error(CodeMsg.SERVER_ERROR);
    }

    @RequestMapping("/thymeleaf")
    public String thymeleaf(Model model) {
        model.addAttribute("name","lingyuhuang");
        return "hello";
    }

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> getDbData() {
        User user = userService.getById(1);
        return Result.success(user);
    }

    @RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> getDbTx() {
        userService.tx();
        return Result.success(true);
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> getRedisKey() {
        User user = redisService.getKey(UserKey.getById,"" + 1,User.class);
        return Result.success(user);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> setRedisKey() {
        User user = new User();
        user.setId(19);
        user.setName("jerry");
        boolean bool = redisService.setKey(UserKey.getById,"" + 1, user);
        return Result.success(bool);
    }

    @RequestMapping("/mq")
    @ResponseBody
    public Result<Boolean> mq(){
        mqSender.send("hello,world");
        return Result.success(true);
    }

}
