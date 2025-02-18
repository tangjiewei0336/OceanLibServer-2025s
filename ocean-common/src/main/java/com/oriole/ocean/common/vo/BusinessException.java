package com.oriole.ocean.common.vo;

import com.oriole.ocean.common.enumerate.ResultCode;

public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = -7480022450501760611L;

    /**
     * 异常码
     */
    private String code;

    /**
     * 异常提示信息
     */
    private String message;

    public BusinessException() {
    }

    public BusinessException(ResultCode resultCode) {
        this.code = resultCode.getCode().toString();
        this.message = resultCode.getMsg();
    }

    public BusinessException(String code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
