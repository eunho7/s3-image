package com.example.image.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageUploadResponse {
    private Long imageSeq;
    private String imageUrl;
    private String originalFileName;
    private Long fileSize;
    private String contentType;
}
