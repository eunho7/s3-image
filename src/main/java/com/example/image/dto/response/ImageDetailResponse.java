package com.example.image.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageDetailResponse {
    private Long imageSeq;
    private String imageUrl;
    private String originalFileName;
    private String storedFileName;
    private String contentType;
    private Long fileSize;
    private String altText;
    private String description;
    private String useYn;
    private LocalDateTime createDttm;
    private LocalDateTime updateDttm;
}
