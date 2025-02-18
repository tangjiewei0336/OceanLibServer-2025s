package com.oriole.ocean.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.oriole.ocean.gateway.tools.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class UserAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<UserAuthGatewayFilterFactory.Config> {

    @Autowired
    JwtUtils jwtUtils;

    public UserAuthGatewayFilterFactory() {
        super(Config.class);
        log.info("Loaded GatewayFilterFactory [Authorize]");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new UserAuthGatewayFilter(config);
    }

    public static class Config {
    }

    public class UserAuthGatewayFilter implements GatewayFilter, Ordered {

        private Config config;

        public UserAuthGatewayFilter(Config config) {
            this.config = config;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            ServerHttpResponse originalResponse = exchange.getResponse();
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    return super.writeWith(DataBufferUtils.join(Flux.from(body))
                            .map(dataBuffer -> {

                                // 必须这样写否则会导致响应不全
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);
                                return content;

                            }).flatMap(bytes -> {

                            //responseData就是下游系统返回的内容,可以查看修改
                            String responseData = new String(bytes, StandardCharsets.UTF_8);
                            JSONObject loginResponseObject = JSONObject.parseObject(responseData);

                            if(loginResponseObject.get("state").equals("SUCCESS") && loginResponseObject.get("code").equals("1")){
                                JSONObject loginInfo = loginResponseObject.getJSONObject("msg");

                                Map<String, Object> chaim = new HashMap<>();
                                chaim.put("username", loginInfo.getString("username"));
                                chaim.put("role", loginInfo.getString("role"));

                                String token = jwtUtils.encode(loginInfo.getString("username"), 24 * 60 * 60 * 1000, chaim);

                                loginResponseObject.put("msg", token);

                                byte[] uppedContent = loginResponseObject.toString().getBytes();
                                originalResponse.getHeaders().set(HttpHeaders.AUTHORIZATION, token);
                                originalResponse.getHeaders().setContentLength(uppedContent.length);
                                return Mono.just(bufferFactory.wrap(uppedContent));
                            }else {
                                originalResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
                                return Mono.just(bufferFactory.wrap(bytes));
                            }
                        }));
                }

                @Override
                public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                    return writeWith(Flux.from(body).flatMapSequential(p -> p));
                }
            };
            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        }

        @Override
        public int getOrder() {
            return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 2;
        }
    }

}