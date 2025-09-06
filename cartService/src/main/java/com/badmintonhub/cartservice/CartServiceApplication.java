package com.badmintonhub.cartservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class CartServiceApplication {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder(); // sẽ resolve http://product-service qua Eureka
    }


    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }

}
