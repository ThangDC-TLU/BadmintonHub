package com.badmintonhub.authservice.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.badmintonhub.authservice.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
public class AuthorizationServerConfiguration {

    @Value("${spring.security.oauth2.authorizationserver.issuer}")
    private String issuerUri;

    private final UserDetailsService userDetailsService;
    private final AuthenticationConfiguration authenticationConfiguration;

    public AuthorizationServerConfiguration(UserDetailsService userDetailsService,
                                            AuthenticationConfiguration authenticationConfiguration) {
        this.userDetailsService = userDetailsService;
        this.authenticationConfiguration = authenticationConfiguration;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Cấu hình AuthenticationProvider cho xác thực username/password từ DB
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService); // Lấy user từ DB
        authenticationProvider.setPasswordEncoder(passwordEncoder()); // So sánh mật khẩu đã mã hóa
        return authenticationProvider;
    }

    // Tạo AuthenticationManager để xử lý đăng nhập
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Cấu hình các endpoint OAuth2 (token, authorize, introspect, revoke...)
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(issuerUri) // Địa chỉ định danh của server
                .authorizationEndpoint("/oauth2/v1/authorize")
                .deviceAuthorizationEndpoint("/oauth2/v1/device_authorization")
                .deviceVerificationEndpoint("/oauth2/v1/device_verification")
                .tokenEndpoint("/oauth2/v1/token") // Nơi client lấy token
                .tokenIntrospectionEndpoint("/oauth2/v1/introspect")
                .tokenRevocationEndpoint("/oauth2/v1/revoke")
                .jwkSetEndpoint("/oauth2/v1/jwks") // Cung cấp public key để verify JWT
                .oidcLogoutEndpoint("/connect/v1/logout")
                .oidcUserInfoEndpoint("/connect/v1/userinfo")
                .oidcClientRegistrationEndpoint("/connect/v1/register")
                .build();
    }

    // Custom payload của JWT token
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            UserDetailsImpl user;
            // Lấy thông tin user từ principal
            if (context.getPrincipal().getPrincipal() instanceof UserDetailsImpl)
                user = (UserDetailsImpl) context.getPrincipal().getPrincipal();
            else
                user = (UserDetailsImpl) context.getPrincipal().getDetails();

            // Lấy danh sách quyền
            Set<String> authorities = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            // Nếu là password grant thì thêm quyền vào scope
            if (context.getAuthorizationGrantType().equals(OAuth2PasswordGrantAuthenticationConverter.PASSWORD))
                authorities.forEach(context.getAuthorizedScopes()::add);

            // Thêm thông tin tuỳ chỉnh vào token
            context.getClaims()
                    .claim("id", user.getId())
                    .claim("authorities", authorities);
        };
    }

    // Sinh ra generator để tạo JWT access token + refresh token
    @Bean
    public OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator() {
        NimbusJwtEncoder jwtEncoder;
        try {
            jwtEncoder = new NimbusJwtEncoder(jwkSource());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(tokenCustomizer()); // Thêm custom claims vào token
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
    }

    // Lưu trữ Authorization (ở đây lưu trong memory, thực tế nên dùng DB)
    @Bean
    public OAuth2AuthorizationService authorizationService() {
        return new InMemoryOAuth2AuthorizationService();
    }

    // Đăng ký client cho OAuth2
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("client") // Client ID
                .clientSecret(passwordEncoder().encode("secret")) // Client Secret (mã hóa)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(OAuth2PasswordGrantAuthenticationConverter.PASSWORD) // Password grant
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // Auth code grant
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN) // Refresh token
                .redirectUri("https://oidcdebugger.com/debug") // URI callback khi login thành công
                .tokenSettings(tokenSettings()) // Cấu hình token TTL
                .scope("client-internal") // Scope cho client
                .build();
        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    // Thời gian sống của token
    @Bean
    public TokenSettings tokenSettings() {
        return TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofDays(2)) // Access token sống 2 ngày
                .refreshTokenTimeToLive(Duration.ofDays(5)) // Refresh token sống 5 ngày
                .build();
    }

    // Provider xử lý password grant
    @Bean
    public OAuth2PasswordGrantAuthenticationProvider oAuth2PasswordGrantAuthenticationProvider(
            OAuth2AuthorizationService oAuth2AuthorizationService,
            OAuth2TokenGenerator<? extends OAuth2Token> oAuth2TokenGenerator,
            AuthenticationManager authenticationManager
    ) {
        return new OAuth2PasswordGrantAuthenticationProvider(
                oAuth2AuthorizationService,
                oAuth2TokenGenerator,
                authenticationManager
        );
    }

    // Cấu hình filter chain cho Authorization Server
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        // Cấu hình token endpoint + custom password grant
        authorizationServerConfigurer
                .tokenEndpoint(tokenEndpoint -> {
                    try {
                        tokenEndpoint
                                .accessTokenRequestConverter(new OAuth2PasswordGrantAuthenticationConverter())
                                .authenticationProvider(
                                        oAuth2PasswordGrantAuthenticationProvider(
                                                authorizationService(),
                                                tokenGenerator(),
                                                authenticationManager(authenticationConfiguration)
                                        )
                                );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .oidc(Customizer.withDefaults()); // Bật OpenID Connect

        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()))
                .with(authorizationServerConfigurer, Customizer.withDefaults());

        return http.build();
    }

    // Sinh cặp khóa RSA để ký JWT
    @Bean
    public JWKSource<SecurityContext> jwkSource() throws NoSuchAlgorithmException {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    // Hàm sinh khóa RSA 2048 bit
    private static KeyPair generateRsaKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    // Bean decode JWT
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

}
