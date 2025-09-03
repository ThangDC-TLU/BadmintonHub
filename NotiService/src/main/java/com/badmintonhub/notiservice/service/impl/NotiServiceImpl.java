package com.badmintonhub.notiservice.service.impl;

import com.badmintonhub.notiservice.dto.message.ObjectResponse;
import com.badmintonhub.notiservice.dto.message.RestResponse;
import com.badmintonhub.notiservice.dto.model.NotiDTO;
import com.badmintonhub.notiservice.dto.model.ProductDTO;
import com.badmintonhub.notiservice.dto.request.NotiCreateDTO;
import com.badmintonhub.notiservice.dto.request.NotiUpdateDTO;
import com.badmintonhub.notiservice.entity.Notification;
import com.badmintonhub.notiservice.exception.ResourceNotFoundException;
import com.badmintonhub.notiservice.repository.NotiRepository;
import com.badmintonhub.notiservice.service.NotiService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
public class NotiServiceImpl implements NotiService {
    private final WebClient webClient;
    private final NotiRepository notiRepository;

    public NotiServiceImpl(WebClient webClient, NotiRepository notiRepository) {
        this.webClient = webClient;
        this.notiRepository = notiRepository;
    }

    @Override
    public NotiDTO getNotiWithProduct(long productId) {
        ProductDTO p = null;

        try {
            RestResponse<ProductDTO> resp = webClient.get()
                    .uri("http://localhost:8081/api/v1/products/{id}", productId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                            cr -> cr.bodyToMono(String.class)
                                    .map(body -> new IllegalStateException("product-service " + cr.statusCode() + ": " + body)))
                    .bodyToMono(new ParameterizedTypeReference<RestResponse<ProductDTO>>() {})
                    .timeout(Duration.ofSeconds(3))
                    .block();

            if (resp != null) {
                p = resp.getData();
            }
        } catch (Exception ex) {
            log.warn("Cannot fetch product {}", productId, ex);
        }

        String title;
        String message;

        if (p != null) {
            double finalPrice = computeEffectivePrice(p.getPrice(), p.getDiscountRate());
            String discountText = formatDiscount(p.getDiscountRate());

            title = "🔥 Shock deal: " + safe(p.getName());
            message = "Giá gốc " + p.getPrice()
                    + (discountText.isEmpty() ? "" : " - Giảm " + discountText)
                    + " → Chỉ còn " + finalPrice + "!";
        } else {
            title = "🔥 Shock deal cho sản phẩm #" + productId;
            message = "Ưu đãi hấp dẫn, xem chi tiết sản phẩm để biết giá!";
        }

        NotiDTO noti = new NotiDTO();
        noti.setId(0L); // noti tức thời, chưa lưu DB
        noti.setTitle(title);
        noti.setMessage(message);
        noti.setTimestamp(Instant.now());
        return noti;
    }


    @Override
    public NotiDTO getNotificationById(long notiId) throws ResourceNotFoundException {
        Notification n = notiRepository.findById(notiId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id=" + notiId));
        return toDTO(n);
    }

    @Override
    public ObjectResponse getAllNotifications(Pageable pageable, Specification<Notification> spec) {
        Page<Notification> page = notiRepository.findAll(spec, pageable);

        // Meta
        ObjectResponse.Meta meta = new ObjectResponse.Meta();
        meta.setPage(page.getNumber() + 1);
        meta.setPageSize(page.getSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        // Kết quả
        ObjectResponse res = new ObjectResponse();
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(this::toDTO).toList());
        return res;
    }


    // ====== WRITES ======
    @Override
    @Transactional
    public void deleteNotification(long notiId) throws ResourceNotFoundException {
        if (!notiRepository.existsById(notiId)) {
            throw new ResourceNotFoundException("Notification not found with id=" + notiId);
        }
        notiRepository.deleteById(notiId);
    }

    @Override
    @Transactional
    public NotiDTO createNotification(@Valid NotiCreateDTO notiCreateDTO) {
        Notification n = new Notification();
        n.setTitle(notiCreateDTO.getTitle().trim());
        n.setMessage(notiCreateDTO.getMessage().trim());
        n.setTimestamp(Instant.now());

        n = notiRepository.save(n);
        return toDTO(n);
    }

    @Override
    @Transactional
    public NotiDTO updateNotification(long notiId, @Valid NotiUpdateDTO notiUpdateDTO) throws ResourceNotFoundException {
        Notification n = notiRepository.findById(notiId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id=" + notiId));

        if (notiUpdateDTO.getTitle() != null && !notiUpdateDTO.getTitle().isBlank()) {
            n.setTitle(notiUpdateDTO.getTitle().trim());
        }
        if (notiUpdateDTO.getMessage() != null && !notiUpdateDTO.getMessage().isBlank()) {
            n.setMessage(notiUpdateDTO.getMessage().trim());
        }

        n = notiRepository.save(n);
        return toDTO(n);
    }

    // ====== helper mapping ======
    private NotiDTO toDTO(Notification n) {
        NotiDTO dto = new NotiDTO();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setTimestamp(n.getTimestamp());
        return dto;
    }

    private double computeEffectivePrice(double price, double discountRate) {
        double rate = (discountRate > 1.0) ? (discountRate / 100.0) : discountRate;
        rate = Math.max(0.0, Math.min(rate, 1.0));
        double result = price * (1.0 - rate);
        return result < 0 ? 0 : result;
    }

    private String formatDiscount(double discountRate) {
        if (discountRate <= 0) return "";
        double percent = (discountRate > 1.0) ? discountRate : (discountRate * 100.0);
        return (percent == Math.floor(percent)) ? String.format("%.0f%%", percent) : String.format("%.2f%%", percent);
    }

    private String safe(String s) { return (s == null) ? "" : s; }
}
