package com.example.image.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageListResponse {
    private int count;
    private List<ImageDetailResponse> images;
}
