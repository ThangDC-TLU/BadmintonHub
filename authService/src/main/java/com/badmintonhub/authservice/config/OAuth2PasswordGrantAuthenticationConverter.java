package com.badmintonhub.authservice.config;

import jakarta.servlet.http.HttpServletRequest;
import com.badmintonhub.authservice.utils.OAuth2Utils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Converter cho grant type "password" trong OAuth2.
 *
 * Spring Authorization Server không hỗ trợ sẵn Password Grant,
 * nên ta phải tự tạo converter để đọc dữ liệu từ request và chuyển thành
 * một đối tượng Authentication (OAuth2PasswordGrantAuthenticationToken).
 */
public class OAuth2PasswordGrantAuthenticationConverter implements AuthenticationConverter {
    private static final Logger log = LoggerFactory.getLogger(OAuth2PasswordGrantAuthenticationConverter.class);

    // Định nghĩa Authorization Grant Type "password"
    public static final AuthorizationGrantType PASSWORD = new AuthorizationGrantType("password");

    @Override
    public Authentication convert(HttpServletRequest request) {

        // Lấy giá trị grant_type từ request
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);

        // Nếu không phải grant_type = "password" thì bỏ qua
        if (!PASSWORD.getValue().equals(grantType))
            return null;

        // Lấy thông tin client (đã được xác thực ở bước filter trước đó)
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        // Nếu client chưa xác thực -> báo lỗi INVALID_CLIENT
        if (clientPrincipal == null)
            OAuth2Utils.throwError(OAuth2ErrorCodes.INVALID_CLIENT, OAuth2ParameterNames.CLIENT_ID,
                    OAuth2Utils.ACCESS_TOKEN_REQUEST_ERROR_URI);

        // Lấy toàn bộ parameter từ request dưới dạng MultiValueMap
        MultiValueMap<String, String> parameters = OAuth2Utils.getParameters(request);

        // Lấy username (bắt buộc)
        String username = parameters.getFirst(OAuth2ParameterNames.USERNAME);

        // Lấy password (bắt buộc)
        String password = parameters.getFirst(OAuth2ParameterNames.PASSWORD);

        // Validate username và password:
        // - Không được rỗng
        // - Chỉ được có 1 giá trị duy nhất trong request
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)
                || parameters.get(OAuth2ParameterNames.USERNAME).size() != 1
                || parameters.get(OAuth2ParameterNames.PASSWORD).size() != 1)
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);

        // Lấy scope (không bắt buộc)
        String scope = parameters.getFirst(OAuth2ParameterNames.SCOPE);

        // Validate scope nếu có: chỉ được có 1 giá trị
        if (scope != null && !StringUtils.hasText(scope) && parameters.get(OAuth2ParameterNames.SCOPE).size() != 1)
            OAuth2Utils.throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.SCOPE,
                    OAuth2Utils.ACCESS_TOKEN_REQUEST_ERROR_URI);

        // Chuyển scope từ chuỗi sang Set<String>
        Set<String> scopes = scope != null ? Set.of(scope.split(" ")) : null;

        // Lưu các parameter bổ sung (ngoài grant_type, scope) để dùng sau
        Map<String, Object> additionalParameters = new HashMap<>();
        parameters.forEach((key, value) -> {
            if (!key.equals(OAuth2ParameterNames.GRANT_TYPE) &&
                    !key.equals(OAuth2ParameterNames.SCOPE)
            ) additionalParameters.put(key, value.get(0));
        });

        if (log.isTraceEnabled()) {
            log.trace("OAuth2 password grant request - Client: {}, Username: {}, GrantType: {}, Scopes: {}",
                    (clientPrincipal != null ? clientPrincipal.getName() : "unknown"),
                    username,
                    grantType,
                    scopes);
        }


        // Tạo token authentication cho grant type password
        return new OAuth2PasswordGrantAuthenticationToken(
                OAuth2PasswordGrantAuthenticationConverter.PASSWORD, // grant type password
                clientPrincipal,                                     // client đã xác thực
                additionalParameters,                                // params bổ sung
                username,                                            // username người dùng
                password,                                            // password người dùng
                scopes                                                // danh sách scope
        );
    }
}

