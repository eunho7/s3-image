CREATE TABLE tb_image_attachment (
    image_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '이미지 시퀀스',
    original_file_name VARCHAR(255) NOT NULL COMMENT '원본 파일명',
    stored_file_name VARCHAR(255) NOT NULL COMMENT '저장 파일명(UUID 기반)',
    s3_key VARCHAR(500) NOT NULL COMMENT 'S3 객체 키',
    content_type VARCHAR(100) NOT NULL COMMENT 'MIME 타입',
    file_size BIGINT NOT NULL COMMENT '파일 크기(bytes)',
    alt_text VARCHAR(300) NULL COMMENT '대체 텍스트',
    description VARCHAR(1000) NULL COMMENT '설명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부(Y/N)',
    create_user VARCHAR(100) NOT NULL COMMENT '생성자',
    create_dttm DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    update_user VARCHAR(100) NOT NULL COMMENT '수정자',
    update_dttm DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (image_seq),
    INDEX idx_tb_image_attachment_use_yn (use_yn),
    INDEX idx_tb_image_attachment_create_dttm (create_dttm)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='이미지 첨부 파일 테이블';
