package com.studify.service;

import com.studify.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final long MAX_SIZE_BYTES = 2 * 1024 * 1024L; // 2MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png"
    );

    @Value("${studify.upload.dir:uploads/profile-pictures}")
    private String uploadDir;

    @Value("${studify.upload.base-url:http://localhost:8080/uploads/profile-pictures}")
    private String baseUrl;

    public String storeProfilePicture(MultipartFile file) {
        // Validar tipo
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("Formato inválido. Apenas JPEG e PNG são aceitos.");
        }

        // Validar tamanho
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BusinessException("Arquivo muito grande. Tamanho máximo: 2MB.");
        }

        // Extensão segura
        String extension = contentType.contains("png") ? ".png" : ".jpg";
        String filename = UUID.randomUUID() + extension;

        try {
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            Path targetPath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("Falha ao salvar arquivo: " + e.getMessage());
        }

        return baseUrl + "/" + filename;
    }
}
