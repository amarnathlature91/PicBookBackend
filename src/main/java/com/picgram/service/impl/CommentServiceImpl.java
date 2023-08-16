package com.picgram.service.impl;

import com.picgram.exception.CommentNotFoundException;
import com.picgram.model.Comment;
import com.picgram.model.Post;
import com.picgram.model.User;
import com.picgram.repository.CommentRepository;
import com.picgram.response.CommentResponse;
import com.picgram.service.CommentService;
import com.picgram.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository cmre;
    @Autowired
    private UserService ussr;
    @Override
    public Comment getCommentById(Long commentId) {
        return cmre.findById(commentId).orElseThrow(()-> new CommentNotFoundException("Comment Not Found With ID"));
    }

    @Override
    public Comment createNewComment(String content, Post post) {
        User authUser = ussr.getAuthenticatedUser();
        Comment newComment = new Comment();
        newComment.setContent(content);
        newComment.setCreator(authUser);
        newComment.setPost(post);
        newComment.setDateCreated(new Date());
        newComment.setDateLastModified(new Date());
        return cmre.save(newComment);
    }

    @Override
    public Comment updateComment(Long commentId, String content) {
        User authUser = ussr.getAuthenticatedUser();
        Comment targetComment = getCommentById(commentId);
        if (targetComment.getCreator().equals(authUser)) {
            targetComment.setContent(content);
            targetComment.setDateLastModified(new Date());
            return cmre.save(targetComment);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public void deleteComment(Long commentId) {
        User authUser = ussr.getAuthenticatedUser();
        Comment targetComment = getCommentById(commentId);
        if (targetComment.getCreator().equals(authUser)) {
            cmre.deleteById(commentId);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public List<CommentResponse> getPostCommentsPaginate(Post post, Integer page, Integer size) {
        User authUser = ussr.getAuthenticatedUser();
        List<Comment> foundCommentList = cmre.findByPost(
                post,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateCreated"))
        );

        List<CommentResponse> commentResponseList = new ArrayList<>();
        foundCommentList.forEach(comment -> {
            commentResponseList.add( new CommentResponse(comment ));
        });
        return commentResponseList;
    }
}
