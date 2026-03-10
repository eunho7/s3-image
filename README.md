# s3-image

Spring Boot + MyBatis + MySQL + AWS S3 기반 이미지 전용 CRUD REST API 프로젝트입니다.

## 1) 기술 스택
- Spring Boot 3.3.x
- JDK 17
- Gradle
- MySQL
- MyBatis(XML Mapper)
- AWS SDK v2 (S3)
- springdoc-openapi (Swagger UI)

## 2) 패키지 구조

```text
com.example.image
├── config
│   └── AwsS3Config.java
├── controller
│   ├── ImageController.java
│   └── ImageViewController.java
├── dto
│   ├── request
│   │   └── ImageMetadataUpdateRequest.java
│   └── response
│       ├── ApiErrorResponse.java
│       ├── ImageDetailResponse.java
│       ├── ImageListResponse.java
│       └── ImageUploadResponse.java
├── entity
│   └── ImageAttachment.java
├── exception
│   ├── CustomException.java
│   ├── ErrorCode.java
│   └── GlobalExceptionHandler.java
├── mapper
│   └── ImageAttachmentMapper.java
├── service
│   ├── ImageService.java
│   └── ImageServiceImpl.java
└── S3ImageApplication.java
```

## 3) 실행 방법

1. MySQL에 DB 생성
```sql
CREATE DATABASE s3_image CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 테이블 생성
```bash
mysql -u root -p s3_image < docs/schema.sql
```

3. 환경 변수 설정
```bash
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_S3_BUCKET=your_bucket
export IMAGE_PUBLIC_BASE_URL=https://api.myservice.com/view/images
```

4. 애플리케이션 실행
```bash
gradle bootRun
```

5. Swagger UI 접속
- http://localhost:8080/swagger-ui.html

## 4) application.yml 예시

```yaml
cloud:
  aws:
    credentials:
      access-key: ${AWS_ACCESS_KEY_ID}
      secret-key: ${AWS_SECRET_ACCESS_KEY}
    region:
      static: ap-northeast-2
    s3:
      bucket: ${AWS_S3_BUCKET}

app:
  image:
    public-base-url: ${IMAGE_PUBLIC_BASE_URL}
```

## 5) AwsS3Config 전체 코드

```java
@Configuration
public class AwsS3Config {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
```

## 6) 이미지 URL 정책
- DB에는 `s3_key`를 저장하고, API 응답 시 `app.image.public-base-url + / + imageSeq`로 public URL을 생성합니다.
- `image_url` 컬럼은 저장하지 않았습니다.
- 이유: 도메인 변경/게이트웨이 변경 시 DB 마이그레이션 없이 설정 값만 바꿔 대응 가능하기 때문입니다.
- S3 원본 URL은 외부로 노출하지 않고, `/view/images/{imageSeq}` API가 최종 이미지 바이트를 반환합니다.

## 7) 업로드 및 검증 정책
- 허용 확장자: `jpg`, `jpeg`, `png`, `gif`, `webp`
- 허용 content-type: `image/jpeg`, `image/png`, `image/gif`, `image/webp`
- 최대 크기: `5MB`
- 빈 파일 업로드 금지
- S3 Key 규칙: `images/yyyy/MM/dd/{uuid}_{originalFileName}`
- 저장 파일명: `UUID + _ + originalFileName`

## 8) API 명세

### 8.1 POST /api/images
- multipart/form-data
- 파라미터: `file`
- 설명: 이미지 업로드 -> S3 저장 -> DB 메타데이터 저장

응답 예시:
```json
{
  "imageSeq": 1,
  "imageUrl": "https://api.myservice.com/view/images/1",
  "originalFileName": "sample.png",
  "fileSize": 12345,
  "contentType": "image/png"
}
```

### 8.2 GET /api/images/{imageSeq}
- 설명: 이미지 메타데이터 상세 조회

응답 예시:
```json
{
  "imageSeq": 1,
  "imageUrl": "https://api.myservice.com/view/images/1",
  "originalFileName": "sample.png",
  "storedFileName": "uuid_sample.png",
  "contentType": "image/png",
  "fileSize": 12345,
  "altText": "샘플 이미지",
  "description": "에디터 본문 삽입용",
  "useYn": "Y",
  "createDttm": "2026-01-01T12:00:00",
  "updateDttm": "2026-01-01T12:00:00"
}
```

### 8.3 GET /api/images
- 설명: 이미지 목록 조회(활성 이미지)

### 8.4 PUT /api/images/{imageSeq}
- 설명: 메타데이터(altText, description) 수정

요청 예시:
```json
{
  "altText": "새 alt 텍스트",
  "description": "새 설명"
}
```

### 8.5 DELETE /api/images/{imageSeq}
- 설명: S3 객체 삭제 + DB soft delete(use_yn = 'N')
- 선택 방식: **soft delete**
- 이유: 감사 로그/복구 가능성/운영 추적에 유리하고 FK 연계 데이터 보호에 안전

### 8.6 GET /view/images/{imageSeq}
- 설명: 공개 이미지 접근용 엔드포인트
- 동작: DB에서 s3_key 조회 -> S3 조회 -> Content-Type 맞춰 바이트 응답
- 텍스트 에디터 본문에는 이 URL을 삽입

## 9) 예외 처리
`@RestControllerAdvice`에서 공통 처리:
- 파일 없음
- 빈 파일
- 허용되지 않은 확장자
- 허용되지 않은 content-type
- 파일 크기 초과
- S3 업로드 실패
- 조회 대상 없음
- 삭제 대상 없음
- 공개 이미지 조회 대상 없음
- 비활성화된 이미지 접근

에러 응답 예시:
```json
{
  "code": "INVALID_EXTENSION",
  "message": "허용되지 않은 확장자입니다. 허용 확장자: jpg, jpeg, png, gif, webp",
  "timestamp": "2026-01-01T12:00:00"
}
```
