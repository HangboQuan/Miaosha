package com.tencent.miaosha.config;

import com.tencent.miaosha.access.UserContext;
import com.tencent.miaosha.domain.MiaoshaUser;
import com.tencent.miaosha.service.MiaoshaUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    //config类的作用是为了,方便传参,直接在controller中的参数中能够传递miaoshaUser对象
    @Autowired
    MiaoshaUserService miaoshaUserService;
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        Class<?> clazz = methodParameter.getParameterType();
        return clazz == MiaoshaUser.class;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        return UserContext.getUser();
    }

    private String getCookieValue(HttpServletRequest request, String cookieNameToken) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for(Cookie cookie:cookies){
            if(cookie.getName().equals(cookieNameToken)){
                return cookie.getValue();
            }
        }
        return null;
    }
}
