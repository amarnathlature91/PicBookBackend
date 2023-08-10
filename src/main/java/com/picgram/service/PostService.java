package com.picgram.service;

import com.picgram.model.Post;
import org.springframework.web.multipart.MultipartFile;

public interface PostService {

    public Post create(String content, MultipartFile photo);
}
