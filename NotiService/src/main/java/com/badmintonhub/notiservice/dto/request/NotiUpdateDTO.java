package com.badmintonhub.notiservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NotiUpdateDTO {
    private String title;
    private String message;
}
