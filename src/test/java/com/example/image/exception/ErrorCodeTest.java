package com.example.image.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorCodeTest {

    @Test
    void shouldHaveExpectedFileSizeExceededValues() {
        assertEquals("FILE_SIZE_EXCEEDED", ErrorCode.FILE_SIZE_EXCEEDED.getCode());
        assertEquals("파일 크기는 최대 5MB까지 허용됩니다.", ErrorCode.FILE_SIZE_EXCEEDED.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ErrorCode.FILE_SIZE_EXCEEDED.getStatus());
    }

    @Test
    void shouldHaveExpectedImageNotFoundValues() {
        assertEquals("IMAGE_NOT_FOUND", ErrorCode.IMAGE_NOT_FOUND.getCode());
        assertEquals("조회 대상 이미지가 없습니다.", ErrorCode.IMAGE_NOT_FOUND.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ErrorCode.IMAGE_NOT_FOUND.getStatus());
    }
}
