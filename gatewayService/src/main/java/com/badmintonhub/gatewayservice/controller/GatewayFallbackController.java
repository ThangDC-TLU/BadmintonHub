package com.badmintonhub.gatewayservice.controller;

import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
public class GatewayFallbackController {
    @RequestMapping("/__fallback/product")
    public ResponseEntity<Map<String,Object>> productFallback(ServerHttpRequest req) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message","Product service is temporarily unavailable",
                        "path", req.getURI().getPath(),
                        "timestamp", Instant.now().toString()));
    }

    @RequestMapping("/__fallback/cart")
    public ResponseEntity<Map<String,Object>> cartFallback(ServerHttpRequest req) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message","Cart service is busy, please try again",
                        "path", req.getURI().getPath(),
                        "timestamp", Instant.now().toString()));
    }

    @RequestMapping("/__fallback/order")
    public ResponseEntity<Map<String,Object>> orderFallback(ServerHttpRequest req) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message","Order service is temporarily unavailable",
                        "path", req.getURI().getPath(),
                        "timestamp", Instant.now().toString()));
    }

    @RequestMapping("/__fallback/noti")
    public ResponseEntity<Map<String,Object>> notiFallback(ServerHttpRequest req) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message","Notification service is unavailable",
                        "path", req.getURI().getPath(),
                        "timestamp", Instant.now().toString()));
    }

    @RequestMapping("/__fallback/auth")
    public ResponseEntity<Map<String,Object>> authFallback(ServerHttpRequest req) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message","Auth service is unavailable",
                        "path", req.getURI().getPath(),
                        "timestamp", Instant.now().toString()));
    }

    @GetMapping("/__fallback/review")
    public ResponseEntity<Map<String,Object>> reviewFallback(ServerHttpRequest req) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "service", "REVIEW-SERVICE",
                        "path", req.getPath().toString(),
                        "message", "Review service is temporarily unavailable"
                ));
    }

}
