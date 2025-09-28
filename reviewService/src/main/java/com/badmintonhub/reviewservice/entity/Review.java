package com.badmintonhub.reviewservice.entity;

import com.badmintonhub.reviewservice.utils.ReviewStatusEnum;
import jakarta.validation.constraints.*; import lombok.Getter; import lombok.Setter;
import org.springframework.data.annotation.*; import org.springframework.data.mongodb.core.index.*; import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant; import java.util.List;

@Document("reviews")
@CompoundIndexes({
        @CompoundIndex(name="product_user_unique", def="{'productId':1,'userId':1}", unique=true),
        @CompoundIndex(name="product_created_idx", def="{'productId':1,'createdAt':-1}"),
        @CompoundIndex(name="status_prod_created_idx", def="{'status':1,'productId':1,'createdAt':-1}")
})
@Getter @Setter
public class Review {
    @Id private String id;

    @Indexed @NotNull private Long productId;
    @Indexed @NotBlank private String userId;

    @Min(1) @Max(5) private int rating;
    @TextIndexed @Size(max=120)  private String title;
    @TextIndexed @Size(max=3000) private String content;

    @Size(max=10) private List<@Size(max=512) String> images;

    @Indexed private ReviewStatusEnum status = ReviewStatusEnum.APPROVED;

    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;
}
