package com.oriole.ocean.common.vo;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Date;

@Slf4j
public class OceanExceptionHandler {

    @Data
    public static class ErrorMsg {
        private String type;
        private String msg;
        private String methodName;
        private String className;
        private String timestamp;

        public ErrorMsg(String type, String methodName, String className, String msg) {
            this.type = type;
            this.methodName = methodName;
            this.className = className;
            this.msg = msg;
            this.timestamp = new Date().toString();
        }
    }

    public MsgEntity<ErrorMsg> businessExceptionHandler(BusinessException ex) {
        StackTraceElement stackTraceElement = Arrays.stream(ex.getStackTrace()).findFirst().get();
        ErrorMsg errorMsg = new ErrorMsg(
                "BUSINESS_EXCEPTION",
                stackTraceElement.getMethodName(),
                stackTraceElement.getClassName(),
                ex.getMessage());
        log.error(JSONObject.toJSONString(errorMsg));
        return new MsgEntity<>("ERROR", ex.getCode(), errorMsg);
    }

    public MsgEntity<ErrorMsg> systemUnexpectedExceptionHandler(Exception ex) {
        StackTraceElement stackTraceElement = Arrays.stream(ex.getStackTrace()).findFirst().get();
        ErrorMsg errorMsg = new ErrorMsg(
                "INTERNAL_SERVER_ERROR",
                stackTraceElement.getMethodName(),
                stackTraceElement.getClassName(),
                ex.getClass().getName() + ":" + ex.getMessage());
        log.error(JSONObject.toJSONString(errorMsg));
        ex.printStackTrace();
        return new MsgEntity<>("ERROR","-1", errorMsg);
    }
}
