package com.example.image.controller;

import com.example.image.dto.request.ImageMetadataUpdateRequest;
import com.example.image.dto.response.ImageDetailResponse;
import com.example.image.dto.response.ImageListResponse;
import com.example.image.dto.response.ImageUploadResponse;
import com.example.image.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Image API", description = "이미지 CRUD API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "이미지 업로드")
    @PostMapping(consumes = "multipart/form-data")
    public ImageUploadResponse uploadImage(@RequestPart("file") MultipartFile file) {
        return imageService.uploadImage(file);
    }

    @Operation(summary = "이미지 상세 메타데이터 조회")
    @GetMapping("/{imageSeq}")
    public ImageDetailResponse getImageDetail(@PathVariable Long imageSeq) {
        return imageService.getImageDetail(imageSeq);
    }

    @Operation(summary = "이미지 목록 조회")
    @GetMapping
    public ImageListResponse getImageList() {
        return imageService.getImageList();
    }

    @Operation(summary = "이미지 메타데이터 수정")
    @PutMapping("/{imageSeq}")
    public ImageDetailResponse updateMetadata(@PathVariable Long imageSeq,
                                              @Valid @org.springframework.web.bind.annotation.RequestBody ImageMetadataUpdateRequest request) {
        return imageService.updateMetadata(imageSeq, request);
    }

    @Operation(summary = "이미지 삭제")
    @DeleteMapping("/{imageSeq}")
    public void deleteImage(@PathVariable Long imageSeq) {
        imageService.deleteImage(imageSeq);
    }
}
