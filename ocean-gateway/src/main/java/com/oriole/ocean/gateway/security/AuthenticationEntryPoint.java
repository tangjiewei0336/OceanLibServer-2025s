package com.oriole.ocean.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oriole.ocean.common.vo.MsgEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.authentication.HttpBasicServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthenticationEntryPoint extends HttpBasicServerAuthenticationEntryPoint {
    @SneakyThrows
    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json; charset=UTF-8");

        MsgEntity<String> msgEntity = new MsgEntity<>("ERROR", "-2", "UNAUTHORIZED");

        ObjectMapper objectMapper = new ObjectMapper();
        DataBuffer bodyDataBuffer = response.bufferFactory().wrap(objectMapper.writeValueAsBytes(msgEntity));

        return response.writeWith(Mono.just(bodyDataBuffer));
    }
}