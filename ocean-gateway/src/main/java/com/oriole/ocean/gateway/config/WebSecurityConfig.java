package com.oriole.ocean.gateway.config;

import com.oriole.ocean.gateway.security.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import static com.oriole.ocean.gateway.config.CorsConfig.buildConfig;

@Configuration
@Slf4j
public class WebSecurityConfig {
    @Autowired
    AccessDeniedHandler accessDeniedHandler;
    @Autowired
    SecurityRepository securityRepository;

    @Autowired
    AuthenticationEntryPoint authenticationEntryPoint;
    private final String[] path= {
            "/favicon.ico",

            "/serverInfo/**",
            "/pdfjs/**",//开发用，线上不可走该流量

            "/swagger-ui.html/**", //swagger，线上必须禁用
            "/swagger-resources/**", //swagger，线上必须禁用
            "/webjars/**", //swagger，线上必须禁用
            "/v2/**", //swagger，线上必须禁用

            "/userAuth/login", //login接口是可以匿名访问的
            "/userAuth/**Login", //login接口是可以匿名访问的
            "/thirdPartLogin/**", //thirdPartLogin三方登录接口是可以匿名访问的
            "/userInfoService/reg", //reg接口是可以匿名访问的

            "/docInfoService/getFileInfoByFileIDWithAnon", //这个接口在文件不允许匿名访问时返回错误信息

            "/docSearch/**", //搜索，无需登录
            "/comment/getComment", //获取评论无需登录
            "/comment/getCommentReply", //获取批量回复也无需登录

            "/userInfoService/getUserLimitedInfo", //获取用户昵称和头像的有限信息接口
            "/userInfoService/checkSameUsername", //检查相同用户名
            "/qaService/qaFile/downloadFile/**", //下载文件接口，允许匿名访问

            //为了提取下载链接，不在Header携带Token，而变为参数携带临时Token
            "/docFileService/downloadFile",
    };
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .securityContextRepository(securityRepository)
                .authorizeExchange()
                .pathMatchers(path).permitAll()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                // 管理员资源 - 允许 ADMIN 和 SUPERADMIN
                .pathMatchers("/admin/**").hasAnyRole("ADMIN", "SUPERADMIN")
                // 普通用户资源 - 允许所有已认证用户
                .pathMatchers("/userWalletService/**", "/userInfoService/**", "/userFunctionService/**")
                .hasAnyRole("USER", "ADMIN", "SUPERADMIN")
                .anyExchange().authenticated()
                .and()
                .httpBasic()
                .and().exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler) //基于http的接口请求鉴权失败
                .and().cors().configurationSource(corsConfigSource())
                .and().csrf().disable(); //必须支持跨域
        return http.build();
    }

    private CorsConfigurationSource corsConfigSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildConfig());
        return source;
    }
}