// utils/FormatRestResponse.java
package com.badmintonhub.reviewservice.utils;

import com.badmintonhub.reviewservice.dto.message.ObjectResponse;
import jakarta.annotation.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.server.*;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;

@RestControllerAdvice
public class FormatRestResponse implements ResponseBodyAdvice<Object> {
    @Override public boolean supports(MethodParameter returnType, Class converterType){ return true; }

    @Override @Nullable
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType ctype,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        var path = request.getURI().getPath();
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) return body;

        // Không bọc non-JSON hoặc lỗi
        if (!MediaType.APPLICATION_JSON.includes(ctype) && !MediaType.APPLICATION_PROBLEM_JSON.includes(ctype)) return body;
        if (body instanceof ProblemDetail || body instanceof Resource || body instanceof String || body instanceof ObjectResponse<?>)
            return body;

        // Thêm message nếu có annotation
        ApiMessage msg = returnType.getMethodAnnotation(ApiMessage.class);
        if (msg != null) {
            // nếu body là Map (ví dụ controller trả Map items/meta/summary) -> chèn message:
            if (body instanceof Map<?,?> map) {
                return Map.of("message", msg.value(), "data", map);
            }
            return Map.of("message", msg.value(), "data", body);
        }
        return body; // không ép bọc mọi thứ để JSON gọn (thực tế)
    }
}
