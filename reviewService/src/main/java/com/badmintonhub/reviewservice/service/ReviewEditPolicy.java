package com.badmintonhub.reviewservice.service;

import com.badmintonhub.reviewservice.config.ReviewEditPolicyProperties;
import com.badmintonhub.reviewservice.entity.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ReviewEditPolicy {
    private final ReviewEditPolicyProperties props;

    public void ensureCanEdit(Review r) {
        Instant now = Instant.now();
        Instant expiresAt = r.getCreatedAt().plusSeconds(props.getWindowHours() * 3600L);
        if (now.isAfter(expiresAt)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "EDIT_WINDOW_EXPIRED");
        }
        if (r.getEditCount() >= props.getMaxTimes()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "EDIT_LIMIT_REACHED");
        }
    }
}

