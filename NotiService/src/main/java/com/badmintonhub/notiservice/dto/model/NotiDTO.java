package com.badmintonhub.notiservice.dto.model;


import lombok.Data;

import java.time.Instant;

@Data
public class NotiDTO {
    private long id;
    private String message;
    private String title;
    private Instant timestamp;
}
