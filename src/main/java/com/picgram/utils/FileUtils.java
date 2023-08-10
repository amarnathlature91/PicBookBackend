package com.picgram.utils;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Component
public class FileUtils {
    public static String nameFile(MultipartFile multipartFile) {
        String originalFileName =
                StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        Integer fileDotIndex = originalFileName.lastIndexOf('.');
        String fileExtension = originalFileName.substring(fileDotIndex);
        return UUID.randomUUID().toString() + fileExtension;
    }

    //method to save new file
    public static void saveNewFile(String uploadDir, String fileName,MultipartFile multipartFile) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Could not save file");
        }
    }

    public static void updateFile(String uploadDir,
                           String oldFileName,
                           String newFileName,
                           MultipartFile multipartFile) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path oldFilePath = Paths.get(oldFileName);
            Path newFilePath = uploadPath.resolve(newFileName);
            Files.delete(oldFilePath);
            Files.copy(inputStream, newFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Could not update file");
        }
    }

    public static void deleteFile(String uploadDir, String fileName) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        Path filePath = uploadPath.resolve(fileName);
        Files.deleteIfExists(filePath);
    }
}
