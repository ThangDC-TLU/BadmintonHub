package com.badmintonhub.authservice.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.Map;

public class OAuth2Utils {

    // Đường dẫn tham khảo khi trả về lỗi token request theo RFC 6749 section 5.2
    public static final String ACCESS_TOKEN_REQUEST_ERROR_URI =
            "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

    /**
     * Lấy thông tin client (OAuth2ClientAuthenticationToken) đã xác thực
     * từ đối tượng Authentication hiện tại.
     *
     * @param authentication đối tượng Authentication (thường từ SecurityContext)
     * @return OAuth2ClientAuthenticationToken đã xác thực
     * @throws OAuth2AuthenticationException nếu client chưa xác thực hoặc không tồn tại
     */
    public static OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(
            Authentication authentication) {

        OAuth2ClientAuthenticationToken clientPrincipal = null;

        // Kiểm tra principal của Authentication có phải là OAuth2ClientAuthenticationToken không
        if (OAuth2ClientAuthenticationToken.class
                .isAssignableFrom(authentication.getPrincipal().getClass())) {
            clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getPrincipal();
        }

        // Nếu đã xác thực thành công thì trả về, ngược lại ném lỗi "invalid_client"
        if (clientPrincipal != null && clientPrincipal.isAuthenticated()) {
            return clientPrincipal;
        }

        throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
    }

    /**
     * Lấy toàn bộ parameter từ HttpServletRequest dưới dạng MultiValueMap.
     *
     * @param request HTTP request chứa tham số (grant_type, client_id, username, password, ...)
     * @return MultiValueMap<String, String> chứa tất cả các parameter
     */
    public static MultiValueMap<String, String> getParameters(HttpServletRequest request) {
        Map<String, String[]> parametersMap = request.getParameterMap();

        // Tạo MultiValueMap với số lượng key ban đầu bằng số lượng parameter trong request
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>(parametersMap.size());

        // Lặp qua từng parameter và thêm tất cả value vào MultiValueMap
        parametersMap.forEach((key, values) -> {
            if (values.length > 0) {
                Arrays.stream(values).forEach(value -> parameters.add(key, value));
            }
        });
        return parameters;
    }

    /**
     * Ném ra lỗi OAuth2AuthenticationException với thông tin error code, parameter liên quan và link RFC.
     *
     * @param errorCode Mã lỗi OAuth2 (ví dụ: invalid_request, invalid_client)
     * @param parameterName Tên tham số gây lỗi
     * @param errorUri Đường dẫn tới tài liệu RFC mô tả lỗi
     */
    public static void throwError(String errorCode, String parameterName, String errorUri) {
        OAuth2Error error = new OAuth2Error(
                errorCode,
                "OAuth 2.0 Parameter: " + parameterName,
                errorUri
        );
        throw new OAuth2AuthenticationException(error);
    }
}
