package com.mkyuan.fountaingateway.gateway.service;

import com.mkyuan.fountaingateway.gateway.MongoRouteDefinitionLocator;
import com.mkyuan.fountaingateway.gateway.RouteDefinitionRepository;
import com.mkyuan.fountaingateway.gateway.model.GatewayRouteDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteService {
    protected Logger logger = LogManager.getLogger(this.getClass());

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

    public GatewayRouteDefinition saveRoute(GatewayRouteDefinition route) throws Exception {
        try {
            GatewayRouteDefinition savedRoute = repository.save(route);
            routeDefinitionLocator.refresh();
            return savedRoute;
        } catch (Exception e) {
            throw new Exception(">>>>>>save routeDefinition error: "+e.getMessage(),e);
        }
    }

    public void deleteRoute(String id) {
        repository.deleteById(id);
        routeDefinitionLocator.refresh();
    }
}
