package com.mkyuan.fountaingateway.gateway;

import com.mkyuan.fountaingateway.gateway.model.GatewayRouteDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteDefinitionRepository extends MongoRepository<GatewayRouteDefinition, String> {
}
