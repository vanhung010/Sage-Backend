package com.sagares.saga_res_api.common.storage;

import com.cloudinary.Cloudinary;
import com.sagares.saga_res_api.common.exception.UpdateException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryImageService {

    private final Cloudinary cloudinary;

    public UploadResult uploadMenuImage(MultipartFile file) {
        validateImage(file);

        Map<?, ?> result;
        try {
            result = cloudinary.uploader().upload(file.getBytes(), Map.of(
                    "asset_folder", "saga/menus",
                    "resource_type", "image",
                    "allowed_formats", List.of("jpg", "png", "webp")
            ));
        } catch (IOException | RuntimeException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Tải hình ảnh thất bại.", ex);
        }

        if (result == null
                || !(result.get("secure_url") instanceof String url) || url.isBlank()
                || !(result.get("public_id") instanceof String publicId) || publicId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Cloudinary trả về thiếu thông tin ảnh.");
        }

        return new UploadResult(url, publicId);
    }

    public void deleteQuietly(String publicId) {
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(
                    publicId, Map.of("resource_type", "image"));
            Object status = result.get("result");
            if (!"ok".equals(status) && !"not found".equals(status)) {
                log.warn("Cloudinary chưa xóa được ảnh {}", publicId);
            }
        } catch (IOException | RuntimeException ex) {
            log.warn("Không thể dọn ảnh Cloudinary {}", publicId, ex);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new UpdateException("Ảnh không được để trống");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !contentType.startsWith("image/")) {
            throw new UpdateException("File phải là hình ảnh");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new UpdateException("Kích thước file phải nhỏ hơn 5MB");
        }
    }

    public record UploadResult(
            String url,
            String publicId
    ) {}
}