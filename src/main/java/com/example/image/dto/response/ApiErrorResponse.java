package com.example.image.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiErrorResponse {
    private String code;
    private String message;
    private LocalDateTime timestamp;
}
