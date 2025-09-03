package com.badmintonhub.notiservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class NotiCreateDTO {
    @NotBlank
    private String title;

    @NotBlank
    private String message;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant timestamp;
}
