package com.tencent.miaosha.controller;

import com.tencent.miaosha.domain.MiaoshaUser;
import com.tencent.miaosha.utils.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class UserController {

    @RequestMapping("/info")
    @ResponseBody
    public Result<MiaoshaUser> info(MiaoshaUser miaoshaUser){
        return Result.success(miaoshaUser);
    }
}
