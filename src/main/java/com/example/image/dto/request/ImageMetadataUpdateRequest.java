package com.example.image.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageMetadataUpdateRequest {

    @Size(max = 300, message = "altText는 300자를 초과할 수 없습니다.")
    private String altText;

    @Size(max = 1000, message = "description은 1000자를 초과할 수 없습니다.")
    private String description;
}
