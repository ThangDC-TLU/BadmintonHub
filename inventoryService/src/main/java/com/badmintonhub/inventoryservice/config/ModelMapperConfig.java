package com.badmintonhub.inventoryservice.config;

import com.badmintonhub.inventoryservice.dto.request.SkuUpsertRequest;
import com.badmintonhub.inventoryservice.entity.Sku;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mm = new ModelMapper();

        // Không ghi đè giá trị đích bằng null từ source
        mm.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true);

        // Converter: trim tất cả String (null-safe)
        Converter<String, String> trim = ctx -> {
            String s = ctx.getSource();
            if (s == null) return null;
            String t = s.trim();
            return t.isEmpty() ? null : t;
        };
        mm.addConverter(trim);

        // TypeMap riêng: SkuUpsertRequest -> Sku (chuẩn hoá skuCode)
        mm.typeMap(SkuUpsertRequest.class, Sku.class)
                .setPostConverter(ctx -> {
                    Sku dest = ctx.getDestination();
                    if (dest.getSkuCode() != null) {
                        dest.setSkuCode(dest.getSkuCode().toUpperCase());
                    }
                    return ctx.getDestination();
                });

        return mm;
    }
}
