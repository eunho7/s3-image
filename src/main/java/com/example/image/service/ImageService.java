package com.example.image.service;

import com.example.image.dto.request.ImageMetadataUpdateRequest;
import com.example.image.dto.response.ImageDetailResponse;
import com.example.image.dto.response.ImageListResponse;
import com.example.image.dto.response.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public interface ImageService {

    ImageUploadResponse uploadImage(MultipartFile file);

    ImageDetailResponse getImageDetail(Long imageSeq);

    ImageListResponse getImageList();

    ImageDetailResponse updateMetadata(Long imageSeq, ImageMetadataUpdateRequest request);

    void deleteImage(Long imageSeq);

    ImageObjectView getImageObjectForView(Long imageSeq);

    record ImageObjectView(String contentType, byte[] bytes) {
        public static ImageObjectView from(ResponseBytes<GetObjectResponse> responseBytes) {
            return new ImageObjectView(responseBytes.response().contentType(), responseBytes.asByteArray());
        }
    }
}
