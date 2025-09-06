package com.badmintonhub.notiservice.service;

import com.badmintonhub.notiservice.dto.event.OrderPlacedEvent;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import freemarker.template.Configuration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class EmailTemplateRenderer {

    private final Configuration fm;

    public EmailTemplateRenderer(Configuration fm) {
        this.fm = fm; // bean Configuration được Spring Boot auto-config khi dùng starter freemarker
    }

    public String renderOrderPlacedEmail(OrderPlacedEvent order, String companyName, String logoUrl) {
        Map<String, Object> model = new HashMap<>();
        model.put("order", order);
        model.put("companyName", companyName);
        model.put("logoUrl", logoUrl);
        model.put("supportEmail", "support@badmintonhub.com");

        ZoneId zone = ZoneId.of(order.getTimezone() != null ? order.getTimezone() : "Asia/Ho_Chi_Minh");
        DateTimeFormatter fmtDate = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
                .withLocale(Locale.forLanguageTag(order.getLocale() != null ? order.getLocale() : "vi-VN"));
        model.put("createdAtFormatted", fmtDate.format(order.getCreatedAt().atZone(zone)));

        model.put("currencySymbol", order.getCurrency() != null && order.getCurrency().equalsIgnoreCase("VND") ? "₫" : "$");
        model.put("subtotalFormatted", order.getSubtotal().toPlainString());
        model.put("shippingFeeFormatted", order.getShippingFee().toPlainString());
        if (order.getDiscountTotal() != null) model.put("discountTotalFormatted", order.getDiscountTotal().toPlainString());
        if (order.getTaxTotal() != null) model.put("taxTotalFormatted", order.getTaxTotal().toPlainString());
        model.put("grandTotalFormatted", order.getGrandTotal().toPlainString());

        List<Map<String, Object>> items = order.getItems().stream().map(it -> {
            Map<String, Object> m = new HashMap<>();
            m.put("productId", it.getProductId());
            m.put("optionId", it.getOptionId());
            m.put("name", it.getName());
            m.put("optionLabel", it.getOptionLabel());
            m.put("image", it.getImage());
            m.put("quantity", it.getQuantity());
            m.put("unitPriceFormatted", it.getUnitPrice().toPlainString());
            m.put("lineTotalFormatted", it.getLineTotal().toPlainString());
            return m;
        }).toList();
        model.put("items", items);

        try (StringWriter out = new StringWriter()) {
            fm.getTemplate("email/order_placed.ftl").process(model, out);
            return out.toString();
        } catch (Exception e) {
            throw new RuntimeException("Render email error", e);
        }
    }
}