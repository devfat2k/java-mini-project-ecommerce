package com.devfat.mini_ecommerce.service.impl;

import com.devfat.mini_ecommerce.service.StorageService;
import com.devfat.mini_ecommerce.util.FileValidationUtil;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final MinioClient minioClient;
    private final FileValidationUtil fileValidationUtil;

    @Value("${app.minio.bucket}")
    private String bucketName;

    @Value("${app.minio.endpoint}")
    private String endPoint;

    private byte[] resizeImage(MultipartFile file, int maxDimension) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .size(maxDimension, maxDimension)
                .outputQuality(0.85)
                .toOutputStream(outputStream);
        return outputStream.toByteArray();
    }


    @Override
    public String uploadFile(MultipartFile file, String folder, boolean resizeImage) {
        fileValidationUtil.validateImageFile(file);

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String objectName = folder + "/" + UUID.randomUUID() + "." + extension;
       try {
           byte[] dataToUpload = resizeImage
                   ? resizeImage(file, 800)
                   : file.getBytes();

           minioClient.putObject(
                   PutObjectArgs.builder()
                           .bucket(bucketName)
                           .object(objectName)
                           .stream(new ByteArrayInputStream(dataToUpload), dataToUpload.length, -1)
                           .contentType(file.getContentType())
                           .build()
           );
       } catch (IOException e) {
           throw new RuntimeException(e.getMessage());
       } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException e) {
           throw new RuntimeException(e);
       }

        return endPoint + "/" + bucketName + "/" + objectName;
    }
}
