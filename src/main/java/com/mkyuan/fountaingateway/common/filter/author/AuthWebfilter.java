package com.mkyuan.fountaingateway.common.filter.author;

import com.alibaba.fastjson.JSON;
import com.mkyuan.fountaingateway.common.controller.response.ResponseBean;
import com.mkyuan.fountaingateway.common.controller.response.ResponseCodeEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthWebfilter implements WebFilter {
    protected Logger logger = LogManager.getLogger(this.getClass());
    private static final List<String> EXCLUDE_URLS =
            Arrays.asList("/venus/api/admin/login", "/venus/api/admin/logout", "/venus/api/admin/checkUserLogin");

    @Autowired
    private AuthService authService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        // 对于OPTIONS请求，直接放行，让CorsWebFilter处理
        if (HttpMethod.OPTIONS.matches(method)) {
            return chain.filter(exchange);
        }

        String path = request.getURI().getPath();
        logger.info(">>>>>>当前正在请求的api路径为->{}", path);
        // 打印所有请求头，便于调试
        logger.info(">>>>>>当前正在请求的api路径为->{}", path);
        request.getHeaders().forEach((name, values) -> {
            logger.info(">>>>>>请求头: {} = {}", name, values);
        });
        String token="";
        String loginId="";
        // 检查是否为排除的URL
        if (EXCLUDE_URLS.contains(path)) {
            logger.info(">>>>> 请求路径 [{}] 在排除列表中，跳过认证", path);
            return chain.filter(exchange);
        }

        logger.info(">>>>> 请求路径 [{}] 需要进行认证", path);

        // 获取请求头中的token和loginId
         token = request.getHeaders().getFirst("token");
         loginId = request.getHeaders().getFirst("loginId");

        // 验证用户认证信息
        ResponseBean responseBean = authService.checkUserAuth(token, loginId);

        if (responseBean.getCode() == ResponseCodeEnum.SUCCESS.getCode()) {
            logger.info(">>>>> 用户认证通过");
            return chain.filter(exchange);
        } else {
            logger.info(">>>>> 用户认证失败");
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

            byte[] bytes = JSON.toJSONString(responseBean).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        }
    }
}