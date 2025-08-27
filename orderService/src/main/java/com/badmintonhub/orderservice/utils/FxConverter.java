package com.badmintonhub.orderservice.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class FxConverter {

    /** Quy đổi VND -> USD, giữ 2 chữ số thập phân (HALF_UP) */
    public BigDecimal vndToUsd(BigDecimal vndAmount) {
        if (vndAmount == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (vndAmount.signum() <= 0) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal usd = vndAmount.divide( new BigDecimal("25000"), 2, RoundingMode.HALF_UP);
        // PayPal tối thiểu 0.01
        if (usd.compareTo(new BigDecimal("0.01")) < 0) {
            usd = new BigDecimal("0.01");
        }
        return usd;
    }

    /** Chuẩn chuỗi gửi PayPal: "100.00" */
    public String vndToUsdString(BigDecimal vndAmount) {
        return vndToUsd(vndAmount).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
