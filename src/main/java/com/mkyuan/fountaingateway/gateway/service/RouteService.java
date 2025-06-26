package com.mkyuan.fountaingateway.gateway.service;
import com.mkyuan.fountaingateway.gateway.MongoRouteDefinitionLocator;
import com.mkyuan.fountaingateway.gateway.RouteDefinitionRepository;
import com.mkyuan.fountaingateway.gateway.model.GatewayRouteDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteService {
    @Autowired
    private RouteDefinitionRepository repository;

    @Autowired
    private MongoRouteDefinitionLocator routeDefinitionLocator;

    public List<GatewayRouteDefinition> getAllRoutes() {
        return repository.findAll();
    }

    public GatewayRouteDefinition getRoute(String id) {
        return repository.findById(id).orElse(null);
    }

    public GatewayRouteDefinition saveRoute(GatewayRouteDefinition route) {
        GatewayRouteDefinition savedRoute = repository.save(route);
        routeDefinitionLocator.refresh();
        return savedRoute;
    }

    public void deleteRoute(String id) {
        repository.deleteById(id);
        routeDefinitionLocator.refresh();
    }
}
