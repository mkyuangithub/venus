package com.mkyuan.fountaingateway.gateway;
import com.mkyuan.fountaingateway.gateway.model.GatewayRouteDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MongoRouteDefinitionLocator implements RouteDefinitionLocator, ApplicationEventPublisherAware {

    private final RouteDefinitionRepository repository;
    private ApplicationEventPublisher publisher;

    @Autowired
    public MongoRouteDefinitionLocator(RouteDefinitionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<GatewayRouteDefinition> gatewayRouteDefinitions = repository.findAll();
        List<RouteDefinition> routeDefinitions = gatewayRouteDefinitions.stream()
                .map(this::convertToRouteDefinition)
                .collect(Collectors.toList());
        return Flux.fromIterable(routeDefinitions);
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
