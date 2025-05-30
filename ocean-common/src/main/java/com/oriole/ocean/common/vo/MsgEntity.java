package com.oriole.ocean.common.vo;

import com.oriole.ocean.common.enumerate.ResultCode;

public class MsgEntity<T> implements java.io.Serializable {
    private String state;
    private String code;
    private T msg;

    public MsgEntity(String state, String code, T msg) {
        this.state = state;
        this.code = code;
        this.msg = msg;
    }

    public MsgEntity(ResultCode resultCode) {
        this.state = resultCode.getSuccess() ? "SUCCESS" : "ERROR";
        this.code = resultCode.getCode().toString();
        this.msg = (T) resultCode.getMsg();
    }


    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public T getMsg() {
        return msg;
    }

    public void setMsg(T msg) {
        this.msg = msg;
    }
}
