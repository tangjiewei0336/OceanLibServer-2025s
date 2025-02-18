package com.oriole.ocean;

import com.oriole.ocean.common.vo.BusinessException;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.common.vo.OceanExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
@ResponseBody
public class ControllerExceptionHandler extends OceanExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    @Override
    public MsgEntity<OceanExceptionHandler.ErrorMsg> businessExceptionHandler(BusinessException ex) {
        return super.businessExceptionHandler(ex);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR)
    @Override
    public MsgEntity<ErrorMsg> systemUnexpectedExceptionHandler(Exception ex) {
        return super.systemUnexpectedExceptionHandler(ex);
    }
}
