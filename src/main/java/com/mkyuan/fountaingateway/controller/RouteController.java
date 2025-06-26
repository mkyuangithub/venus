package com.mkyuan.fountaingateway.controller;
import com.mkyuan.fountaingateway.gateway.model.GatewayRouteDefinition;
import com.mkyuan.fountaingateway.gateway.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
public class RouteController {
    @Autowired
    private RouteService routeService;

    @GetMapping
    public ResponseEntity<List<GatewayRouteDefinition>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GatewayRouteDefinition> getRoute(@PathVariable String id) {
        GatewayRouteDefinition route = routeService.getRoute(id);
        if (route == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(route);
    }

    @PostMapping
    public ResponseEntity<GatewayRouteDefinition> createRoute(@RequestBody GatewayRouteDefinition route) {
        return ResponseEntity.ok(routeService.saveRoute(route));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GatewayRouteDefinition> updateRoute(@PathVariable String id, @RequestBody GatewayRouteDefinition route) {
        route.setId(id);
        return ResponseEntity.ok(routeService.saveRoute(route));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable String id) {
        routeService.deleteRoute(id);
        return ResponseEntity.ok().build();
    }
}
