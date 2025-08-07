package com.mkyuan.fountaingateway.gateway.service;

import com.mkyuan.fountaingateway.config.redis.RedisMessagePublisher;
import com.mkyuan.fountaingateway.controller.RouteAdminException;
import com.mkyuan.fountaingateway.gateway.MongoRouteDefinitionLocator;
import com.mkyuan.fountaingateway.gateway.RouteDefinitionRepository;
import com.mkyuan.fountaingateway.gateway.model.GatewayRouteDefinition;
import jodd.util.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.mkyuan.fountaingateway.common.RedisKeyConstants;

import java.util.Collections;
import java.util.List;

@Service
public class RouteService {
    protected Logger logger = LogManager.getLogger(this.getClass());
    public final static int PAGESIZE=20;

    @Autowired
    private RouteDefinitionRepository repository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisMessagePublisher redisMessagePublisher;

    @Autowired
    private MongoRouteDefinitionLocator routeDefinitionLocator;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Page<GatewayRouteDefinition> searchRoutes(int pageNumber, int pageSize, String searchedUri){
        String collectionName = "gateway_routes";
        Pageable pageable = PageRequest.of(0, PAGESIZE); // 创建一个分页请求
        Page<GatewayRouteDefinition> userList = new PageImpl<>(Collections.emptyList(), pageable, PAGESIZE);
        try{
            Query query = new Query();
            if(StringUtil.isNotBlank(searchedUri)) {
                query.addCriteria(Criteria.where("uri").regex(".*" + searchedUri + ".*", "i"));
            }
            // 检查 queryDate 是否不为空

            long count = mongoTemplate.count(query, GatewayRouteDefinition.class, collectionName);
            List<GatewayRouteDefinition> users = mongoTemplate.find(query.with(PageRequest.of(pageNumber - 1, pageSize)), GatewayRouteDefinition.class, collectionName);
            return new PageImpl<>(users, PageRequest.of(pageNumber - 1, pageSize), count);
        }catch(Exception e){
            logger.error(">>>>>>list All gateway_routes service error->{}",e.getMessage(),e);
        }
        return userList;
    }

    public void refreshAllDataToRedis(){
        try{
            List<GatewayRouteDefinition> routeList=repository.findAll();
            for(GatewayRouteDefinition route: routeList){
                this.saveRouteDefinitionToRedis(route);
            }

        }catch(Exception e){
            logger.error(">>>>>>refreshAllDataToRedis error->{}",e.getMessage(),e);
        }
    }
    public List<GatewayRouteDefinition> getAllRoutes() {
        return repository.findAll();
    }

    public GatewayRouteDefinition getRoute(String id) {
        return repository.findById(id).orElse(null);
    }

    public GatewayRouteDefinition saveRoute(GatewayRouteDefinition route) throws Exception {
        try {
            GatewayRouteDefinition existedRoute=this.getRoute(route.getId());
            if(existedRoute!=null){
                logger.info(">>>>>>Create route->service id->{} has already existed",route.getId());
                throw new RouteAdminException("route existed");
            }
            String action = "UPDATE";
            GatewayRouteDefinition savedRoute = repository.save(route);
            this.saveRouteDefinitionToRedis(route);
            routeDefinitionLocator.refresh();
            // 发布路由变更消息到Stream
            redisMessagePublisher.publishRouteChangeMessage(action, route.getId());
            return savedRoute;
        }
        catch (RouteAdminException rae) {
            throw new RouteAdminException("route existed");
        }
        catch (Exception e) {
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
