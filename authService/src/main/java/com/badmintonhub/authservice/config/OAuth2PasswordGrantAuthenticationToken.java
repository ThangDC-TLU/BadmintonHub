package com.badmintonhub.authservice.config;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Map;
import java.util.Set;

@Getter
public class OAuth2PasswordGrantAuthenticationToken
        extends OAuth2AuthorizationGrantAuthenticationToken {

    // Username người dùng gửi trong request
    private final String username;

    // Password người dùng gửi trong request
    private final String password;

    // Danh sách scope (quyền hạn) yêu cầu
    private final Set<String> scopes;

    /**
     * Constructor tạo ra token cho Password Grant Type.
     *
     * @param authorizationGrantType Kiểu grant type (ở đây luôn là "password").
     * @param clientPrincipal         Thông tin xác thực của client (ứng dụng) đang yêu cầu token.
     * @param additionalParameters    Các tham số bổ sung (ngoài username, password, scope).
     * @param username                Tên đăng nhập của người dùng.
     * @param password                Mật khẩu của người dùng.
     * @param scopes                  Danh sách quyền hạn (nếu có).
     */
    protected OAuth2PasswordGrantAuthenticationToken(
            AuthorizationGrantType authorizationGrantType,
            Authentication clientPrincipal,
            Map<String, Object> additionalParameters,
            String username,
            String password,
            Set<String> scopes
    ) {
        // Gọi constructor cha với grant_type = "password"
        super(OAuth2PasswordGrantAuthenticationConverter.PASSWORD, clientPrincipal, additionalParameters);

        // Lưu thông tin đăng nhập và scope
        this.username = username;
        this.password = password;
        this.scopes = scopes;
    }
}

