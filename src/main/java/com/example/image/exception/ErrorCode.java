package com.example.image.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    FILE_NOT_FOUND("FILE_NOT_FOUND", "파일이 존재하지 않습니다.", HttpStatus.BAD_REQUEST),
    EMPTY_FILE("EMPTY_FILE", "빈 파일은 업로드할 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_EXTENSION("INVALID_EXTENSION", "허용되지 않은 확장자입니다.", HttpStatus.BAD_REQUEST),
    INVALID_CONTENT_TYPE("INVALID_CONTENT_TYPE", "허용되지 않은 Content-Type 입니다.", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED("FILE_SIZE_EXCEEDED", "파일 크기는 최대 5MB까지 허용됩니다.", HttpStatus.BAD_REQUEST),
    S3_UPLOAD_FAILED("S3_UPLOAD_FAILED", "S3 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    IMAGE_NOT_FOUND("IMAGE_NOT_FOUND", "조회 대상 이미지가 없습니다.", HttpStatus.NOT_FOUND),
    DELETE_TARGET_NOT_FOUND("DELETE_TARGET_NOT_FOUND", "삭제 대상 이미지가 없습니다.", HttpStatus.NOT_FOUND),
    PUBLIC_IMAGE_NOT_FOUND("PUBLIC_IMAGE_NOT_FOUND", "공개 이미지 조회 대상이 없습니다.", HttpStatus.NOT_FOUND),
    IMAGE_INACTIVE("IMAGE_INACTIVE", "비활성화된 이미지는 접근할 수 없습니다.", HttpStatus.FORBIDDEN),
    S3_READ_FAILED("S3_READ_FAILED", "S3 이미지 조회에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    S3_DELETE_FAILED("S3_DELETE_FAILED", "S3 이미지 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
