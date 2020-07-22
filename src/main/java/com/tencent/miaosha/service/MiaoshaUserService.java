package com.tencent.miaosha.service;

import com.tencent.miaosha.dao.MiaoshaUserDao;
import com.tencent.miaosha.domain.MiaoshaUser;
import com.tencent.miaosha.exception.GlobalException;
import com.tencent.miaosha.redis.MiaoshaUserKey;
import com.tencent.miaosha.redis.RedisService;
import com.tencent.miaosha.utils.CodeMsg;
import com.tencent.miaosha.utils.MD5Utils;
import com.tencent.miaosha.utils.UUIDUtil;
import com.tencent.miaosha.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoshaUserService {


    @Autowired(required = false)
    MiaoshaUserDao miaoshaUserDao;

    @Autowired
    RedisService redisService;
    public MiaoshaUser getMiaoshaUserById(long id) {
        return miaoshaUserDao.getMiaoshaUserById(id);
    }

    public static final String COOKIE_NAME_TOKEN = "token";
    public boolean login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
            //return CodeMsg.SERVER_ERROR;
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        //判断手机号是否存在
        MiaoshaUser miaoshaUser = getMiaoshaUserById(Long.parseLong(mobile));
        if (miaoshaUser == null) {
            //return CodeMsg.MOBILE_NOT_EXISTS;
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXISTS);
        }

        String dbPass = miaoshaUser.getPassword();
        String salt = miaoshaUser.getSalt();
        String calcPass = MD5Utils.formPassToDBPass(formPass,salt);
        if (!calcPass.equals(dbPass)) {
            //return CodeMsg.PASSWORD_ERROR;
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }

        /*String token = UUIDUtil.uuid();
        redisService.setKey(MiaoshaUserKey.token,token,miaoshaUser);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);*/
        addCookie(response,miaoshaUser);
        return true;
    }


    public MiaoshaUser getById(long id){
        //查缓存
        MiaoshaUser miaoshaUser = redisService.getKey(MiaoshaUserKey.getById,""+id,MiaoshaUser.class);
        if(miaoshaUser != null){
            return miaoshaUser;
        }
        //查数据库
        miaoshaUser = miaoshaUserDao.getMiaoshaUserById(id);
        if(miaoshaUser == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXISTS);
        }
        redisService.setKey(MiaoshaUserKey.getById,""+id,miaoshaUser);
        return miaoshaUser;
    }


    public boolean updatePassword(String token,long id,String formPass){

        //获取user
        MiaoshaUser miaoshaUser = getById(id);
        if(miaoshaUser == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXISTS);
        }
        //更新数据库
        MiaoshaUser miaoshaUser1 = new MiaoshaUser();
        miaoshaUser1.setId(id);
        miaoshaUser1.setPassword(MD5Utils.formPassToDBPass(formPass,miaoshaUser.getSalt()));
        miaoshaUserDao.update(miaoshaUser1);
        //更新缓存
        redisService.delete(MiaoshaUserKey.getById, ""+id);
        miaoshaUser.setPassword(miaoshaUser1.getPassword());
        redisService.setKey(MiaoshaUserKey.token, token, miaoshaUser);

        return true;
    }
    public MiaoshaUser getByToken(HttpServletResponse response,String token){
        if(StringUtils.isEmpty(token)){
            return null;
        }
        MiaoshaUser miaoshaUser = redisService.getKey(MiaoshaUserKey.token,token,MiaoshaUser.class);
        if(miaoshaUser != null){
            addCookie(response,miaoshaUser);
        }
        return miaoshaUser;
    }

    private void addCookie(HttpServletResponse response, MiaoshaUser miaoshaUser){
        String token = UUIDUtil.uuid();
        redisService.setKey(MiaoshaUserKey.token,token,miaoshaUser);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
