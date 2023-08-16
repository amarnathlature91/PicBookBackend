package com.picgram.service;

import com.picgram.model.Comment;
import com.picgram.model.Post;
import com.picgram.response.CommentResponse;

import java.util.List;

public interface CommentService {
    Comment getCommentById(Long commentId);
    Comment createNewComment(String content, Post post);
    Comment updateComment(Long commentId, String content);
    void deleteComment(Long commentId);
    List<CommentResponse> getPostCommentsPaginate(Post post, Integer page, Integer size);
}
