package com.yupi.springbootinit.controller;
import com.yupi.springbootinit.common.BaseResponse;
import cn.hutool.core.io.FileUtil;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.config.MinioConfiguration;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.entity.User;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/image")
@Slf4j
public class ImageController {

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioConfiguration minioConfiguration;

    @PostMapping("/upload")
    public BaseResponse<String> uploadBlogImage(@RequestPart MultipartFile file) {
        try {
            // 校验文件类型和大小
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "只能上传图片文件");
            }
            long maxSize = 2 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 2MB");
            }

            // 处理文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            if (StringUtils.isBlank(extension)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的文件类型");
            }
            String fileName = String.format("team_image/%s%s", UUID.randomUUID(), extension);

            // 上传到 MinIO
            try (InputStream inputStream = file.getInputStream()) { // 确保流关闭
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(minioConfiguration.getBucket())
                                .object(fileName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(contentType)
                                .build());
            }

            // 构建访问 URL
            String avatarUrl = String.format("%s/%s/%s",
                    minioConfiguration.getEndpoint(),
                    minioConfiguration.getBucket(),
                    fileName);
            return ResultUtils.success(avatarUrl);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败: " + e.getMessage());
        }
    }
    private String getFileExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".")))
                .orElse("");
    }
}