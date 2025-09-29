package com.badmintonhub.reviewservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "review.edit")
@Getter
@Setter
public class ReviewEditPolicyProperties {
    private int maxTimes = 1;
    private int windowHours = 48;
}

