package com.oriole.ocean.gateway.security;

import com.alibaba.fastjson.JSONObject;
import com.oriole.ocean.gateway.tools.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;

import static org.springframework.security.core.context.SecurityContextHolder.*;

@Slf4j
@Component
public class SecurityRepository implements ServerSecurityContextRepository {

    @Autowired
    JwtUtils jwtUtils;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        /* 取得JWT */
        String jwt = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        /* 验证JWT */
        if (jwt != null && jwtUtils.isVerify(jwt)) {
            /* 解析JWT */
            Claims claims = jwtUtils.decode(jwt);

            JSONObject authInfo = new JSONObject();
            authInfo.put("username", claims.get("username"));
            authInfo.put("role", claims.get("role"));

            /* 重设头信息 */
            exchange.getRequest().mutate().headers(httpHeaders ->
                    httpHeaders.set(HttpHeaders.AUTHORIZATION, authInfo.toString())
            );

            /* 传递Authentication给SpringSecurity */
            Collection<SimpleGrantedAuthority> authorities=new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + authInfo.getString("role")));

            Authentication authentication=new UsernamePasswordAuthenticationToken(null, null, authorities);

            SecurityContext emptyContext = createEmptyContext();
            emptyContext.setAuthentication(authentication);
            return Mono.just(emptyContext);
        } else {
            return Mono.empty();
        }
    }
}