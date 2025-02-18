package com.oriole.ocean.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oriole.ocean.common.vo.MsgEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component

public class AccessDeniedHandler implements ServerAccessDeniedHandler {
    @SneakyThrows
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json; charset=UTF-8");

        MsgEntity<String> msgEntity = new MsgEntity<>("ERROR", "-2", "FORBIDDEN");
        log.error("access forbidden path={}", exchange.getRequest().getPath());

        ObjectMapper objectMapper = new ObjectMapper();
        DataBuffer dataBuffer = response.bufferFactory().wrap(objectMapper.writeValueAsBytes(msgEntity));
        return response.writeWith(Mono.just(dataBuffer));
    }
}