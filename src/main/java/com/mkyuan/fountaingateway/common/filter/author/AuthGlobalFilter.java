package com.mkyuan.fountaingateway.common.filter.author;

import com.mkyuan.fountaingateway.common.controller.response.ResponseBean;
import com.mkyuan.fountaingateway.common.controller.response.ResponseCodeEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.fastjson.JSON;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered{
    protected Logger logger = LogManager.getLogger(this.getClass());
    private static final List<String> EXCLUDE_URLS = Arrays.asList("/api/admin/login", "/api/admin/logout");

   @Autowired
    private AuthService authService;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        logger.info(">>>>>>当前正在请求的api路径为->{}",path);
        // 检查是否为排除的URL
        if (EXCLUDE_URLS.contains(path)) {
            logger.info(">>>>> 请求路径 [{}] 在排除列表中，跳过认证", path);
            return chain.filter(exchange);
        }

        logger.info(">>>>> 请求路径 [{}] 需要进行认证", path);

        // 获取请求头中的token和loginId
        String token = request.getHeaders().getFirst("token");
        String loginId = request.getHeaders().getFirst("loginId");

        // 验证用户认证信息
        ResponseBean responseBean = authService.checkUserAuth(token, loginId);

        if (responseBean.getCode() == ResponseCodeEnum.SUCCESS.getCode()) {
            logger.info(">>>>> 用户认证通过");
            return chain.filter(exchange);
        } else {
            logger.info(">>>>> 用户认证失败");
            return responseError(exchange, responseBean);
        }
    }

    private Mono<Void> responseError(ServerWebExchange exchange, ResponseBean responseBean) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        byte[] bytes = JSON.toJSONString(responseBean).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1; // 设置过滤器优先级，数字越小优先级越高
    }
}
