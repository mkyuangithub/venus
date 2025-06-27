package com.mkyuan.fountaingateway.gateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mkyuan.fountaingateway.common.RedisKeyConstants;
import com.mkyuan.fountaingateway.gateway.model.GatewayRouteDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
@Component
public class MongoRouteDefinitionLocator implements RouteDefinitionLocator, ApplicationEventPublisherAware {
    private ObjectMapper objectMapper = new ObjectMapper();
    protected Logger logger = LogManager.getLogger(this.getClass());
    private final RouteDefinitionRepository repository;
    private ApplicationEventPublisher publisher;

    @Autowired
    public MongoRouteDefinitionLocator(RouteDefinitionRepository repository) {
        this.repository = repository;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        //List<GatewayRouteDefinition> gatewayRouteDefinitions = repository.findAll(); //从mongodb中获取所有的路由信息
        List<GatewayRouteDefinition> gatewayRouteDefinitions=this.getRouteDefinitionFromRedis();
        List<RouteDefinition> routeDefinitions = gatewayRouteDefinitions.stream()
                .map(this::convertToRouteDefinition)
                .collect(Collectors.toList());
        return Flux.fromIterable(routeDefinitions);
    }
    private List<GatewayRouteDefinition> getRouteDefinitionFromRedis(){
        List<GatewayRouteDefinition> allDefinitions=new ArrayList<>();
        try{
            String redisKey = RedisKeyConstants.Gateway_Router_Definition_Key;
            // 获取hash中的所有值
            List<Object> values = redisTemplate.opsForHash().values(redisKey);

            if (values != null && !values.isEmpty()) {
                for (Object value : values) {
                    if (value instanceof String) {
                        // 将JSON字符串反序列化为GatewayRouteDefinition对象
                        GatewayRouteDefinition definition = objectMapper.readValue((String)value, GatewayRouteDefinition.class);
                        allDefinitions.add(definition);
                    } else {
                        // 尝试直接转换
                        try {
                            String jsonString = objectMapper.writeValueAsString(value);
                            GatewayRouteDefinition definition = objectMapper.readValue(jsonString, GatewayRouteDefinition.class);
                            allDefinitions.add(definition);
                        } catch (Exception e) {
                            logger.warn("无法转换路由定义: {}", value);
                        }
                    }
                }
            }
            logger.info("从Redis获取到{}条路由定义", allDefinitions.size());
        }catch(Exception e){
            logger.error(">>>>>>getAllDefinition from redis error: {}",e.getMessage(),e);
        }
        return allDefinitions;
    }
    private RouteDefinition convertToRouteDefinition(GatewayRouteDefinition gatewayRouteDefinition) {
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(gatewayRouteDefinition.getId());
        routeDefinition.setUri(URI.create(gatewayRouteDefinition.getUri()));
        routeDefinition.setOrder(gatewayRouteDefinition.getOrder());

        List<PredicateDefinition> predicateDefinitions = new ArrayList<>();
        for (PredicateDefinition pd : gatewayRouteDefinition.getPredicates()) {
            PredicateDefinition predicateDefinition = new PredicateDefinition();
            predicateDefinition.setName(pd.getName());
            predicateDefinition.setArgs(pd.getArgs());
            predicateDefinitions.add(predicateDefinition);
        }
        routeDefinition.setPredicates(predicateDefinitions);

        List<FilterDefinition> filterDefinitions = new ArrayList<>();
        for (FilterDefinition fd : gatewayRouteDefinition.getFilters()) {
            FilterDefinition filterDefinition = new FilterDefinition();
            filterDefinition.setName(fd.getName());
            filterDefinition.setArgs(fd.getArgs());
            filterDefinitions.add(filterDefinition);
        }
        routeDefinition.setFilters(filterDefinitions);

        return routeDefinition;
    }

    public void refresh() {
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}
