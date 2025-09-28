package com.badmintonhub.reviewservice.controller;

import com.badmintonhub.reviewservice.dto.*; import com.badmintonhub.reviewservice.dto.message.ObjectResponse;
import com.badmintonhub.reviewservice.service.ReviewService; import com.badmintonhub.reviewservice.utils.ApiMessage;
import com.badmintonhub.reviewservice.utils.CustomHeaders;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/product/{productId}")
    @ApiMessage("GET_REVIEWS_SUCCESS")
    public ObjectResponse<ReviewsPayload> list(@PathVariable Long productId,
                                               @RequestParam(defaultValue="1") int page,
                                               @RequestParam(defaultValue="10") int size,
                                               @RequestParam(defaultValue="createdAt,desc") String sort) {
        return this.reviewService.listPaged(productId, page-1, size, sort);
    }

    @PostMapping
    @ApiMessage("CREATE_REVIEW_SUCCESS")
    public ResponseEntity<ReviewDTO> create(@Valid @RequestBody CreateReview req,
                                            @RequestHeader(CustomHeaders.X_AUTH_USER_ID) String userId){
        var dto = reviewService.create(req, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    @ApiMessage("UPDATE_REVIEW_SUCCESS")
    public ReviewDTO update(@PathVariable String id, @Valid @RequestBody UpdateReview req,
                            @RequestHeader(CustomHeaders.X_AUTH_USER_ID) String userId){
        return reviewService.update(id, req, userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id,
                                       @RequestHeader(CustomHeaders.X_AUTH_USER_ID) String userId){
        reviewService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
