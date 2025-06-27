package com.mkyuan.fountaingateway.controller;
import com.mkyuan.fountaingateway.gateway.model.GatewayRouteDefinition;
import com.mkyuan.fountaingateway.gateway.service.RouteService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/routes")
public class RouteController {
    protected Logger logger = LogManager.getLogger(this.getClass());
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
        try {
            logger.info(">>>>>>create a new route successfully");
            return ResponseEntity.ok(routeService.saveRoute(route));
        }catch(Exception e){
            logger.error(">>>>>>create routeDefinition error: {}",e.getMessage(),e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GatewayRouteDefinition> updateRoute(@PathVariable String id, @RequestBody GatewayRouteDefinition route) {
        route.setId(id);
        try {
            logger.info(">>>>>>update a route successfully");
            return ResponseEntity.ok(routeService.saveRoute(route));
        }catch(Exception e){
            logger.error(">>>>>>save routeDefinition error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>>  deleteRoute(@PathVariable String id) {
        routeService.deleteRoute(id);
        Map<String, String> response = new HashMap<>();
        response.put("status", "successful");
        return ResponseEntity.ok(response);
    }
}
