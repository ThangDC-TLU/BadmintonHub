package com.badmintonhub.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "skus")
public class Sku {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã SKU duy nhất (do ProductService sinh), ví dụ: PROD123-RED-3U */
    @Column(nullable = false, unique = true, length = 100)
    private String skuCode;

    /** Tham chiếu mềm sang ProductService */
    private Long productId;

    /** Tên hiển thị, ví dụ "Vợt A - Đỏ - 3U" */
    @Column(nullable = false, length = 255)
    private String name;

    /** Ảnh chụp tổ hợp option (JSON string) */
    @Column(columnDefinition = "TEXT")
    private String optionJson;

    @Column(length = 64, unique = true)
    private String barcode;

    private Integer weightGram;   // >= 0
    private Integer widthMm;      // >= 0
    private Integer heightMm;     // >= 0
    private Integer lengthMm;     // >= 0

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    private Instant createdAt;

}
