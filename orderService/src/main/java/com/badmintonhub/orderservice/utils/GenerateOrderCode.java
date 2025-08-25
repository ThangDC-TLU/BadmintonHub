package com.badmintonhub.orderservice.utils;

public class GenerateOrderCode {
    private static final java.time.format.DateTimeFormatter TS_FMT =
            java.time.format.DateTimeFormatter.ofPattern("yyMMddHHmmss");

    public static String generateOrderCode() {
        String ts = java.time.LocalDateTime.now().format(TS_FMT);
        String suf = java.util.UUID.randomUUID().toString().replace("-", "")
                .substring(0, 6).toUpperCase(); // 6 ký tự hex
        return "OD" + ts + suf;
    }

}
