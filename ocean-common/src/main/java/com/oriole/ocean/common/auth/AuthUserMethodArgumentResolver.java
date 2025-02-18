package com.oriole.ocean.common.auth;

import com.alibaba.fastjson.JSONObject;
import com.oriole.ocean.common.vo.AuthUserEntity;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthUserMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUser.class) &&
                parameter.getParameterType().isAssignableFrom(AuthUserEntity.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                                  NativeWebRequest request, WebDataBinderFactory factory) {
        if (parameter.hasParameterAnnotation(AuthUser.class)) {
            // Header中获取由网关校验的权限信息
            JSONObject authorizationInfo = JSONObject.parseObject(request.getHeader(HttpHeaders.AUTHORIZATION));
            return new AuthUserEntity(authorizationInfo.getString("username"), authorizationInfo.getString("role"));
        }
        return null;
    }
}
