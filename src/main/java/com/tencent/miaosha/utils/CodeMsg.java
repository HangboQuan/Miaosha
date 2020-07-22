package com.tencent.miaosha.utils;

public class CodeMsg {

    private int code;
    private String msg;

    //通用异常，并且可以扩展：5001XX
    public static CodeMsg SUCCESS = new CodeMsg(0, "success");
    public static CodeMsg SERVER_ERROR = new CodeMsg(500101, "服务器异常");
    public static CodeMsg BIND_ERROR = new CodeMsg(500102, "参数校验异常：%s");
    public static CodeMsg REQUEST_ILLEGAL = new CodeMsg(500103, "请求非法");
    public static CodeMsg ACCESS_LIMIT_REACHED = new CodeMsg(500104, "频繁访问");
    //登录异常：5002XX
    public static CodeMsg MOBILE_EMPTY = new CodeMsg(500201, "手机号为空");
    public static CodeMsg PASSWORD_EMPTY = new CodeMsg(500202, "密码为空");
    public static CodeMsg MOBILE_ERROR = new CodeMsg(500203, "手机号格式错误");
    public static CodeMsg MOBILE_NOT_EXISTS = new CodeMsg(500204, "手机号不存在");
    public static CodeMsg PASSWORD_ERROR = new CodeMsg(500205, "密码错误");
    public static CodeMsg SESSION_ERROR = new CodeMsg(500210, "Session不存在或者已经失效");

    //订单异常：5003XX
    public static CodeMsg ORDER_NOT_EXIST = new CodeMsg(500300,"订单信息不存在");

    //秒杀异常：5005XX
    public static CodeMsg MIAO_SHA_OVER = new CodeMsg(500500, "秒杀库存清零");
    public static CodeMsg REPEAT_MIAO_SHA = new CodeMsg(500501, "重复秒杀");
    public static CodeMsg MIAOSHA_FAIL = new CodeMsg(500502, "秒杀失败");

    public CodeMsg fillArgs(Object... args) {
        int code = this.code;
        String message = String.format(this.msg, args);
        return new CodeMsg(code, message);
    }
    private CodeMsg(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
