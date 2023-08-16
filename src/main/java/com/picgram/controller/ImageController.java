package com.picgram.controller;

import com.picgram.utils.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/uploads",method = RequestMethod.GET,
        produces = MediaType.IMAGE_JPEG_VALUE)
public class ImageController {
    private static final Map<String, MediaType> TYPE_MAP = new HashMap<>();

    static {
        TYPE_MAP.put("pdf", MediaType.APPLICATION_PDF);
        TYPE_MAP.put("gif", MediaType.IMAGE_GIF);
        TYPE_MAP.put("jpg", MediaType.IMAGE_JPEG);
        TYPE_MAP.put("jpeg", MediaType.IMAGE_JPEG);
        TYPE_MAP.put("png", MediaType.IMAGE_PNG);
        TYPE_MAP.put("html", MediaType.TEXT_HTML);
        TYPE_MAP.put("xls", MediaType.parseMediaType("application/vnd.ms-excel"));
        TYPE_MAP.put("xlsx", MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }


    @GetMapping(value = "/profiles/{imageName}")
    public ResponseEntity<?> previewProfileFile(@PathVariable String imageName) throws MalformedURLException, IOException {
        String filePath="uploads/profiles/"+imageName;
        Path path = Paths.get(filePath);
        File imgFile=new File(path.toString());
        byte[] bytes = FileCopyUtils.copyToByteArray(imgFile);
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(bytes);
    }

    @GetMapping(value = "/posts/{imageName}")
    public ResponseEntity<?> previewPostFile(@PathVariable String imageName) throws MalformedURLException, IOException {
        String filePath="uploads/posts/"+imageName;
        Path path = Paths.get(filePath);
        File imgFile=new File(path.toString());
        byte[] bytes = FileCopyUtils.copyToByteArray(imgFile);
        System.out.println(filePath);
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(bytes);
    }
}
