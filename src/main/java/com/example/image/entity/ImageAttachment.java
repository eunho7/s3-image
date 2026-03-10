package com.example.image.entity;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ImageAttachment {
    private Long imageSeq;
    private String originalFileName;
    private String storedFileName;
    private String s3Key;
    private String contentType;
    private Long fileSize;
    private String altText;
    private String description;
    private String useYn;
    private String createUser;
    private LocalDateTime createDttm;
    private String updateUser;
    private LocalDateTime updateDttm;
}
