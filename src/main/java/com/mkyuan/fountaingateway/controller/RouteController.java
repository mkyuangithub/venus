package com.mkyuan.fountaingateway.controller;

import com.alibaba.fastjson.JSONObject;
import com.mkyuan.fountaingateway.common.controller.response.ResponseBean;
import com.mkyuan.fountaingateway.common.controller.response.ResponseCodeEnum;
import com.mkyuan.fountaingateway.gateway.model.GatewayRouteDefinition;
import com.mkyuan.fountaingateway.gateway.service.RouteService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/venus") // 添加统一的基础路径
public class RouteController {
    protected Logger logger = LogManager.getLogger(this.getClass());
    @Autowired
    private RouteService routeService;

    @GetMapping
    public ResponseEntity<List<GatewayRouteDefinition>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @GetMapping("/api/admin/route/searchRoutes")
    public ResponseBean searchRoutes(@RequestHeader("token") String token, @RequestHeader("loginId") String loginId,
                                     @RequestParam(value = "searchedUri", required = false, defaultValue = "")
                                     String searchedUri,
                                     @RequestParam(value = "pageNumber", required = false, defaultValue = "1")
                                     int pageNumber,
                                     @RequestParam(value = "pageSize", required = false, defaultValue = "10")
                                     int pageSize) {
        Pageable pageable = PageRequest.of(0, RouteService.PAGESIZE); // 创建一个分页请求
        Page<GatewayRouteDefinition> routeList = new PageImpl<>(Collections.emptyList(), pageable, 0);
        routeList = routeService.searchRoutes(pageNumber, pageSize, searchedUri);
        return new ResponseBean(ResponseCodeEnum.SUCCESS, routeList);
    }

    @GetMapping("/api/admin/route/getRouteById")
    public ResponseBean getRoute(
            @RequestParam(value = "routeId", required = true, defaultValue = "") String routeId) {
        GatewayRouteDefinition route = routeService.getRoute(routeId);
        if (route == null) {
            return new ResponseBean(ResponseCodeEnum.SUCCESS,new GatewayRouteDefinition());
        }
        return new ResponseBean(ResponseCodeEnum.SUCCESS,route);
    }

    @PostMapping("/api/admin/route/create")
    public ResponseBean createRoute(@RequestBody GatewayRouteDefinition route) {
        try {
            logger.info(">>>>>>create a new route successfully");
            GatewayRouteDefinition newRoute = routeService.saveRoute(route);
            return new ResponseBean(ResponseCodeEnum.SUCCESS, newRoute);
        } catch (RouteAdminException rae) {
            return new ResponseBean(ResponseCodeEnum.ILLEGAL_PARAMETERS.getCode(),
                    "创建路由失败，因为路由的service id己存在!", null);
        } catch (Exception e) {
            logger.error(">>>>>>create routeDefinition error: {}", e.getMessage(), e);
            return new ResponseBean(ResponseCodeEnum.FAIL);
        }
    }

    @PostMapping("/api/admin/route/update")
    public ResponseBean updateRoute(@RequestBody GatewayRouteDefinition route) {
        try {
            logger.info(">>>>>>update a route successfully");
            routeService.updateRoute(route);
            return new ResponseBean(ResponseCodeEnum.SUCCESS,route);
        } catch (Exception e) {
            logger.error(">>>>>>save routeDefinition error: {}", e.getMessage(), e);
            return new ResponseBean(ResponseCodeEnum.FAIL);
        }
    }

    @PostMapping("/api/admin/route/delete")
    public ResponseBean deleteRoute(@RequestBody JSONObject params) {
        List<String> ids = params.getJSONArray("ids").toJavaList(String.class);
        routeService.deleteRoute(ids);
        Map<String, String> response = new HashMap<>();
        response.put("status", "successful");
        return new ResponseBean(ResponseCodeEnum.SUCCESS);
    }
}
