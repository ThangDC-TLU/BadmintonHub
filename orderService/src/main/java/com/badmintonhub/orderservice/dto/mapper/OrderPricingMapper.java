package com.badmintonhub.orderservice.dto.mapper;

import com.badmintonhub.orderservice.dto.model.ProductItemBriefDTO;
import com.badmintonhub.orderservice.entity.Order;
import com.badmintonhub.orderservice.entity.OrderItem;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class OrderPricingMapper {

    @Getter
    public static class Totals {
        private final BigDecimal subtotal;
        private final BigDecimal discount;
        public Totals(BigDecimal subtotal, BigDecimal discount) {
            this.subtotal = subtotal;
            this.discount = discount;
        }
    }

    public Totals attachCartItems(Order order, List<ProductItemBriefDTO> dtos) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;

        for (ProductItemBriefDTO dto : dtos) {
            if (dto == null) continue;

            ensureComputedPrices(dto);
            int qty = Math.max(1, dto.getQuantity());

            double unitFinalD      = dto.getFinalPrice();
            double unitDiscountedD = dto.getDiscountedFinalPrice();
            double lineFinalD      = dto.getLineFinalPrice() > 0 ? dto.getLineFinalPrice() : unitFinalD * qty;
            double lineDiscountedD = dto.getLineDiscountedFinalPrice() > 0
                    ? dto.getLineDiscountedFinalPrice() : unitDiscountedD * qty;

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .productId(dto.getProductId())
                    .optionId(dto.getOptionId())
                    .nameSnapshot(dto.getName())
                    .imageSnapshot(dto.getImage())
                    .optionLabelSnapshot(dto.getOptionLabel())
                    .quantity(qty)

                    .unitBasePrice(bd(dto.getBasePrice()))
                    .unitAddPrice(bd(dto.getAddPrice()))
                    .unitSubPrice(bd(dto.getSubPrice()))
                    .unitFinalPrice(bd(unitFinalD))
                    .unitDiscountPercent(bd(dto.getDiscountPercent()))
                    .unitDiscountedPrice(bd(unitDiscountedD))

                    .lineFinalPrice(bd(lineFinalD))
                    .lineDiscountedPrice(bd(lineDiscountedD))
                    .build();

            order.getItems().add(item);

            subtotal = subtotal.add(item.getLineFinalPrice());
            discount = discount.add(item.getLineFinalPrice().subtract(item.getLineDiscountedPrice()));
        }
        return new Totals(subtotal, discount);
    }

    /** finalPrice = base + add - sub; discountedFinal = finalPrice * (1 - discount%/100) */
    private static void ensureComputedPrices(ProductItemBriefDTO p) {
        if (p.getFinalPrice() <= 0.0) {
            p.setFinalPrice(p.getBasePrice() + p.getAddPrice() - p.getSubPrice());
        }
        if (p.getDiscountedFinalPrice() <= 0.0) {
            double factor = 1.0 - (p.getDiscountPercent() / 100.0);
            if (factor < 0) factor = 0;
            p.setDiscountedFinalPrice(p.getFinalPrice() * factor);
        }
    }

    private static BigDecimal bd(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }
}
