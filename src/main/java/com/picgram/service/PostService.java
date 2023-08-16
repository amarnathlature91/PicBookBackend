package com.picgram.service;

import com.picgram.model.Comment;
import com.picgram.model.Post;
import com.picgram.model.User;
import com.picgram.response.PostResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {

    Post getPostById(Long postId);
    PostResponse getPostResponseById(Long postId);
    List<PostResponse> getPostsByUserPaginate(User author, Integer page, Integer size);
    List<PostResponse> getTimelinePostsPaginate(Integer page, Integer size);
    Post createNewPost(String description, MultipartFile postPhoto);
    Post updatePost(Long postId, String content);
    void deletePost(Long postId);
    void likePost(Long postId);
    void unlikePost(Long postId);
    Comment createPostComment(Long postId, String content);
    Comment updatePostComment(Long commentId, Long postId, String content);
    void deletePostComment(Long commentId, Long postId);
}
