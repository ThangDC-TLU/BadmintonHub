package com.badmintonhub.cartservice.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;


@Configuration
public class WebClientConfig {

    /**
     * WebClient dùng để gọi product-service trực tiếp (local).
     * Mặc định baseUrl = http://localhost:8081, có thể override qua application.yml:
     *
     * clients:
     *   product:
     *     base-url: http://localhost:8081
     */
    @Bean(name = "productWebClient")
    public WebClient productWebClient(
            @Value("${clients.product.base-url:http://localhost:8081/api/v1/products}") String baseUrl
    ) {
        // Cấu hình timeout cơ bản khi test nội bộ
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(5))
                        .addHandlerLast(new WriteTimeoutHandler(5)));

        return WebClient.builder()
                .baseUrl(baseUrl) // ví dụ: http://localhost:8081
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}