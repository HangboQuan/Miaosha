package com.tencent.miaosha.utils;

public class Result<T>{

    private int code;
    private String msg;
    private T data;

    public Result(T data){
        this.code = 0;
        this.msg = "success";
        this.data = data;
    }

    public Result(CodeMsg cm) {
        if (null == cm){
            return ;
        }
        this.code = cm.getCode();
        this.msg = cm.getMsg();
    }

    //成功调用的时候
    public static <T> Result<T> success(T data) {
        return new Result<T>(data);
    }


    //失败调用的时候
    public static <T> Result<T> error(CodeMsg cm) {
        return new Result<T>(cm);
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }





}
