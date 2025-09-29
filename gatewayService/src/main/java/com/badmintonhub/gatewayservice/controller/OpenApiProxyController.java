package com.badmintonhub.gatewayservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/openapi")
public class OpenApiProxyController {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${auth.token-url}")
    private String tokenUrl;

    @Value("${auth.scopes}")
    private String scopesCsv;

    public OpenApiProxyController(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/product", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> product() { return forwardAndPatch("http://PRODUCT-SERVICE/v3/api-docs"); }

    @GetMapping(value = "/order", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> order() { return forwardAndPatch("http://ORDER-SERVICE/v3/api-docs"); }

    @GetMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> auth() { return forwardAndPatch("http://AUTH-SERVICE/v3/api-docs"); } // nếu user API ở auth-service

    @GetMapping(value = "/cart", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> cart() { return forwardAndPatch("http://CART-SERVICE/v3/api-docs"); } // nếu user API ở auth-service

    @GetMapping(value = "/noti", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> user() { return forwardAndPatch("http://NOTI-SERVICE/v3/api-docs"); } // nếu user API ở auth-service

    @GetMapping(value = "/review", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> review() { return forwardAndPatch("http://REVIEW-SERVICE/v3/api-docs"); } // nếu user API ở auth-service


    private Mono<String> forwardAndPatch(String uri) {
        return webClientBuilder.build().get().uri(uri).retrieve()
                .bodyToMono(String.class)
                .map(this::patchOpenApi);
    }

    private String patchOpenApi(String json) {
        try {
            ObjectNode root = (ObjectNode) objectMapper.readTree(json);

            // 1) ép security (password flow)
            ObjectNode flows = root.with("components").with("securitySchemes")
                    .with("oauth2").put("type", "oauth2").with("flows");
            ObjectNode pwd = flows.with("password");
            pwd.put("tokenUrl", tokenUrl);
            ObjectNode scopes = pwd.with("scopes");
            for (String s : scopesCsv.split("\\s*,\\s*")) scopes.put(s, "Scope " + s);

            // 2) ép security requirement toàn cục
            ArrayNode security = root.withArray("security");
            ObjectNode oauthReq = objectMapper.createObjectNode();
            ArrayNode reqScopes = oauthReq.putArray("oauth2");
            for (String s : scopesCsv.split("\\s*,\\s*")) reqScopes.add(s);
            security.add(oauthReq);

            // 3) QUAN TRỌNG: ép servers -> Gateway (để Try it out không gọi port nội bộ)
            ArrayNode servers = objectMapper.createArrayNode();
            ObjectNode server = objectMapper.createObjectNode();
            server.put("url", "http://localhost:8085");     // ví dụ http://localhost:8085
            servers.add(server);
            root.set("servers", servers);

            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return json; // lỗi parse thì trả nguyên
        }
    }
}

