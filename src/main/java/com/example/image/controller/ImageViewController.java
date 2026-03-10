package com.example.image.controller;

import com.example.image.service.ImageService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/view/images")
public class ImageViewController {

    private final ImageService imageService;

    @GetMapping("/{imageSeq}")
    public ResponseEntity<byte[]> viewImage(@PathVariable Long imageSeq) {
        ImageService.ImageObjectView image = imageService.getImageObjectForView(imageSeq);
        MediaType mediaType = MediaType.parseMediaType(image.contentType());

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                .contentType(mediaType)
                .body(image.bytes());
    }
}
