package com.mkyuan.fountaingateway.gateway.service;

import com.mkyuan.fountaingateway.config.redis.RedisMessagePublisher;
import com.mkyuan.fountaingateway.gateway.MongoRouteDefinitionLocator;
import com.mkyuan.fountaingateway.gateway.RouteDefinitionRepository;
import com.mkyuan.fountaingateway.gateway.model.GatewayRouteDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.mkyuan.fountaingateway.common.RedisKeyConstants;
import java.util.List;

@Service
public class RouteService {
    protected Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    private RouteDefinitionRepository repository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisMessagePublisher redisMessagePublisher;

    @Autowired
    private MongoRouteDefinitionLocator routeDefinitionLocator;

    public List<GatewayRouteDefinition> getAllRoutes() {
        return repository.findAll();
    }

    public GatewayRouteDefinition getRoute(String id) {
        return repository.findById(id).orElse(null);
    }

    public GatewayRouteDefinition saveRoute(GatewayRouteDefinition route) throws Exception {
        try {
            String action = "UPDATE";
            GatewayRouteDefinition savedRoute = repository.save(route);
            this.saveRouteDefinitionToRedis(route);
            routeDefinitionLocator.refresh();
            // 发布路由变更消息到Stream
            redisMessagePublisher.publishRouteChangeMessage(action, route.getId());
            return savedRoute;
        } catch (Exception e) {
            throw new Exception(">>>>>>save routeDefinition error: "+e.getMessage(),e);
        }
    }

    public void deleteRoute(String id) {
        repository.deleteById(id);
        this.delRouteDefinitionFromRedis(id);
        routeDefinitionLocator.refresh();
        // 发布路由删除消息到Stream
        redisMessagePublisher.publishRouteChangeMessage("DELETE", id);
    }

    private void saveRouteDefinitionToRedis(GatewayRouteDefinition route){
        String redisKey=RedisKeyConstants.Gateway_Router_Definition_Key;
        redisTemplate.opsForHash().put(redisKey,route.getId(),route);
    }

    private void delRouteDefinitionFromRedis(String id){
        String redisKey=RedisKeyConstants.Gateway_Router_Definition_Key;
        redisTemplate.opsForHash().delete(redisKey,id);
    }
}
