package com.example.image.service;

import com.example.image.dto.request.ImageMetadataUpdateRequest;
import com.example.image.dto.response.ImageDetailResponse;
import com.example.image.dto.response.ImageListResponse;
import com.example.image.dto.response.ImageUploadResponse;
import com.example.image.entity.ImageAttachment;
import com.example.image.exception.CustomException;
import com.example.image.exception.ErrorCode;
import com.example.image.mapper.ImageAttachmentMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;

    private final ImageAttachmentMapper imageAttachmentMapper;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${app.image.public-base-url}")
    private String publicBaseUrl;

    @Override
    @Transactional
    public ImageUploadResponse uploadImage(MultipartFile file) {
        validateImageFile(file);

        String originalFileName = file.getOriginalFilename();
        getExtension(originalFileName);
        String uuid = UUID.randomUUID().toString();
        String storedFileName = uuid + "_" + sanitizeFilename(originalFileName);

        LocalDate today = LocalDate.now();
        String s3Key = String.format("images/%d/%02d/%02d/%s",
                today.getYear(), today.getMonthValue(), today.getDayOfMonth(), storedFileName);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (Exception exception) {
            throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
        }

        ImageAttachment image = ImageAttachment.builder()
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .s3Key(s3Key)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .altText(null)
                .description(null)
                .useYn("Y")
                .createUser("system")
                .updateUser("system")
                .build();

        imageAttachmentMapper.insertImage(image);

        return ImageUploadResponse.builder()
                .imageSeq(image.getImageSeq())
                .imageUrl(buildImagePublicUrl(image.getImageSeq()))
                .originalFileName(image.getOriginalFileName())
                .fileSize(image.getFileSize())
                .contentType(image.getContentType())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ImageDetailResponse getImageDetail(Long imageSeq) {
        ImageAttachment image = imageAttachmentMapper.findByImageSeq(imageSeq);
        if (image == null) {
            throw new CustomException(ErrorCode.IMAGE_NOT_FOUND);
        }
        return toImageDetailResponse(image);
    }

    @Override
    @Transactional(readOnly = true)
    public ImageListResponse getImageList() {
        List<ImageDetailResponse> images = imageAttachmentMapper.findAllActiveImages().stream()
                .map(this::toImageDetailResponse)
                .toList();

        return ImageListResponse.builder()
                .count(images.size())
                .images(images)
                .build();
    }

    @Override
    @Transactional
    public ImageDetailResponse updateMetadata(Long imageSeq, ImageMetadataUpdateRequest request) {
        ImageAttachment image = imageAttachmentMapper.findByImageSeq(imageSeq);
        if (image == null) {
            throw new CustomException(ErrorCode.IMAGE_NOT_FOUND);
        }
        if (!"Y".equals(image.getUseYn())) {
            throw new CustomException(ErrorCode.IMAGE_INACTIVE);
        }

        ImageAttachment update = ImageAttachment.builder()
                .imageSeq(imageSeq)
                .altText(request.getAltText())
                .description(request.getDescription())
                .updateUser("system")
                .build();

        imageAttachmentMapper.updateMetadata(update);

        ImageAttachment updated = imageAttachmentMapper.findByImageSeq(imageSeq);
        return toImageDetailResponse(updated);
    }

    @Override
    @Transactional
    public void deleteImage(Long imageSeq) {
        ImageAttachment image = imageAttachmentMapper.findByImageSeq(imageSeq);
        if (image == null) {
            throw new CustomException(ErrorCode.DELETE_TARGET_NOT_FOUND);
        }
        if (!"Y".equals(image.getUseYn())) {
            throw new CustomException(ErrorCode.DELETE_TARGET_NOT_FOUND);
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(image.getS3Key())
                    .build());
        } catch (Exception exception) {
            throw new CustomException(ErrorCode.S3_DELETE_FAILED);
        }

        imageAttachmentMapper.softDeleteImage(imageSeq, "system");
    }

    @Override
    @Transactional(readOnly = true)
    public ImageObjectView getImageObjectForView(Long imageSeq) {
        ImageAttachment image = imageAttachmentMapper.findByImageSeq(imageSeq);
        if (image == null) {
            throw new CustomException(ErrorCode.PUBLIC_IMAGE_NOT_FOUND);
        }
        if (!"Y".equals(image.getUseYn())) {
            throw new CustomException(ErrorCode.IMAGE_INACTIVE);
        }

        try {
            var responseBytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(image.getS3Key())
                    .build());
            return ImageObjectView.from(responseBytes);
        } catch (NoSuchKeyException exception) {
            throw new CustomException(ErrorCode.PUBLIC_IMAGE_NOT_FOUND);
        } catch (Exception exception) {
            throw new CustomException(ErrorCode.S3_READ_FAILED);
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_FILE);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        String originalFileName = file.getOriginalFilename();
        String extension = getExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new CustomException(ErrorCode.INVALID_EXTENSION,
                    "허용되지 않은 확장자입니다. 허용 확장자: jpg, jpeg, png, gif, webp");
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE,
                    "허용되지 않은 Content-Type 입니다. 허용 타입: image/jpeg, image/png, image/gif, image/webp");
        }
    }

    private String getExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            throw new CustomException(ErrorCode.INVALID_EXTENSION,
                    "확장자 없는 파일은 업로드할 수 없습니다.");
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private String sanitizeFilename(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String buildImagePublicUrl(Long imageSeq) {
        return publicBaseUrl.endsWith("/") ? publicBaseUrl + imageSeq : publicBaseUrl + "/" + imageSeq;
    }

    private ImageDetailResponse toImageDetailResponse(ImageAttachment image) {
        return ImageDetailResponse.builder()
                .imageSeq(image.getImageSeq())
                .imageUrl(buildImagePublicUrl(image.getImageSeq()))
                .originalFileName(image.getOriginalFileName())
                .storedFileName(image.getStoredFileName())
                .contentType(image.getContentType())
                .fileSize(image.getFileSize())
                .altText(image.getAltText())
                .description(image.getDescription())
                .useYn(image.getUseYn())
                .createDttm(image.getCreateDttm())
                .updateDttm(image.getUpdateDttm())
                .build();
    }
}
