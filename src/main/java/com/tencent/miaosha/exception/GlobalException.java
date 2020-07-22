package com.tencent.miaosha.exception;

import com.tencent.miaosha.utils.CodeMsg;

public class GlobalException extends RuntimeException {

    private CodeMsg cm;

    public GlobalException(CodeMsg cm) {
        this.cm = cm;
    }

    public CodeMsg getCm() {
        return cm;
    }

    public void setCm(CodeMsg cm) {
        this.cm = cm;
    }
}
