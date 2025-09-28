package com.badmintonhub.reviewservice.service;

import com.badmintonhub.reviewservice.dto.CreateReview;
import com.badmintonhub.reviewservice.dto.ReviewDTO;
import com.badmintonhub.reviewservice.dto.ReviewsPayload;
import com.badmintonhub.reviewservice.dto.UpdateReview;
import com.badmintonhub.reviewservice.dto.message.ObjectResponse;
import com.badmintonhub.reviewservice.entity.Review;
import com.badmintonhub.reviewservice.repository.ReviewRepository;
import com.badmintonhub.reviewservice.utils.ReviewStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository repo;

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
        return ReviewDTO.from(saved);
    }

    public ReviewDTO update(String id, UpdateReview req, String userId) {
        Review current = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!current.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        current.setRating(req.rating());
        current.setTitle(req.title());
        current.setContent(req.content());
        current.setUpdatedAt(Instant.now());

        Review saved = repo.save(current);
        return ReviewDTO.from(saved);
    }

    public void delete(String id, String userId) {
        Review current = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!current.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        repo.deleteById(id);
    }

    public ObjectResponse<ReviewsPayload> listPaged(Long productId, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<ReviewDTO> pageData = repo
                .findByProductIdAndStatus(productId, ReviewStatusEnum.APPROVED.name(), pageable)
                .map(ReviewDTO::from);

        List<ReviewRepository.ProductAgg> agg = repo.aggProduct(productId);
        double avg = agg.isEmpty() ? 0.0 : agg.get(0).getAvgRating();
        int cnt = agg.isEmpty() ? 0 : agg.get(0).getCount();

        return ObjectResponse.fromPage(pageData, sort,
                p -> new ReviewsPayload(p.getContent(), avg, cnt));
    }
}
