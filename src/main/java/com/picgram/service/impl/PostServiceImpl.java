package com.picgram.service.impl;

import com.picgram.model.Post;
import com.picgram.repository.PostRepository;
import com.picgram.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository prp;

    @Override
    public Post create(String content, MultipartFile photo) {


        return null;
    }
}
