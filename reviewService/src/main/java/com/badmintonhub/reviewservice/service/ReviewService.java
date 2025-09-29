package com.badmintonhub.reviewservice.service;

import com.badmintonhub.reviewservice.dto.*;
import com.badmintonhub.reviewservice.dto.message.ObjectResponse;
import com.badmintonhub.reviewservice.entity.Review;
import com.badmintonhub.reviewservice.repository.ReviewRepository;
import com.badmintonhub.reviewservice.utils.ReviewStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository repo;
    private final ReviewAggregationService aggregationService;
    private final ReviewEditPolicy editPolicy;


    public ReviewDTO create(CreateReview req, String userId) {
        repo.findByProductIdAndUserId(req.productId(), userId).ifPresent(x -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ALREADY_REVIEWED");
        });

        Review review = new Review();
        review.setProductId(req.productId());
        review.setUserId(userId);
        review.setRating(req.rating());
        review.setTitle(req.title());
        review.setContent(req.content());
        review.setImages(req.images());
        review.setStatus(ReviewStatusEnum.APPROVED);

        Review saved = repo.save(review);
        aggregationService.recomputeAndPublish(saved.getProductId());   // <-- GỬI KAFKA SAU KHI TẠO
        return ReviewDTO.from(saved);
    }

    public ReviewDTO update(String id, UpdateReview req, String userId) {
        Review current = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!current.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        editPolicy.ensureCanEdit(current);

        int oldRating = current.getRating();

        boolean changed = false;
        if (current.getRating() != req.rating()) { current.setRating(req.rating()); changed = true; }
        if (!Objects.equals(current.getTitle(), req.title())) { current.setTitle(req.title()); changed = true; }
        if (!Objects.equals(current.getContent(), req.content())) { current.setContent(req.content()); changed = true; }

        if (!changed) return ReviewDTO.from(current);

        current.setUpdatedAt(Instant.now());
        current.setEditCount(current.getEditCount() + 1);

        Review saved = repo.save(current);

        if (saved.getRating() != oldRating) {
            aggregationService.recomputeAndPublish(saved.getProductId());
        }
        return ReviewDTO.from(saved);
    }

    public void delete(String id, String userId) {
        Review current = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!current.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Long productId = current.getProductId();
        repo.deleteById(id);
        aggregationService.recomputeAndPublish(productId);             // <-- GỬI KAFKA SAU KHI XOÁ
    }

    public ObjectResponse<ReviewsPayload> listPaged(Long productId, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<ReviewDTO> pageData = repo
                .findByProductIdAndStatus(productId, ReviewStatusEnum.APPROVED.name(), pageable)
                .map(ReviewDTO::from);

        List<ProductAgg> agg = repo.aggProduct(productId);
        double avg = agg.isEmpty() ? 0.0 : agg.get(0).getAvgRating();
        int cnt = agg.isEmpty() ? 0 : agg.get(0).getCount();

        return ObjectResponse.fromPage(pageData, sort,
                p -> new ReviewsPayload(p.getContent(), avg, cnt));
    }
}
