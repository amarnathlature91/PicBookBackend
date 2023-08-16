package com.picgram.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.picgram.model.Comment;
import com.picgram.model.Post;
import com.picgram.model.User;
import com.picgram.response.CommentResponse;
import com.picgram.response.PostResponse;
import com.picgram.service.CommentService;
import com.picgram.service.PostService;
import com.picgram.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:8181")
public class PostController {
    @Autowired
    private PostService postService;
    @Autowired
    private CommentService cmsr;
    @Autowired
    private UserService ussr;

    @GetMapping( "/feed")
    public ResponseEntity<?> getTimelinePosts(@RequestParam("page") Integer page,
                                              @RequestParam("size") Integer size) {
        page = page < 0 ? 0 : page-1;
        size = size <= 0 ? 5 : size;
        List<PostResponse> timelinePosts = postService.getTimelinePostsPaginate(page, size);
        return new ResponseEntity<>(timelinePosts, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createNewPost(@RequestParam(value = "content", required = true) Optional<String> content,
                                           @RequestParam(name = "postPhoto", required = true) Optional<MultipartFile> postPhoto) throws JsonProcessingException {
        if ((content.isEmpty() || content.get().length() == 0) &&
                (postPhoto.isEmpty() || postPhoto.get().getSize() <= 0)) {
            throw new RuntimeException();
        }
        String contentToAdd = content.orElse(null);
        MultipartFile postPhotoToAdd = postPhoto.orElse(null);

        Post createdPost = postService.createNewPost(contentToAdd, postPhotoToAdd);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    @PostMapping("/{postId}/update")
    public ResponseEntity<?> updatePost(@PathVariable("postId") Long postId,
                                        @RequestParam(value = "content", required = false) Optional<String> content) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        String contentToAdd = content.orElse(null);

        Post updatePost = postService.updatePost(postId, contentToAdd);
        return new ResponseEntity<>(updatePost, HttpStatus.OK);
    }

    @PostMapping("/{postId}/delete")
    public ResponseEntity<?> deletePost(@PathVariable("postId") Long postId) {
        postService.deletePost(postId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable("postId") Long postId) {
        PostResponse foundPostResponse = postService.getPostResponseById(postId);
        return new ResponseEntity<>(foundPostResponse, HttpStatus.OK);
    }

    @GetMapping("/{postId}/likes")
    public ResponseEntity<?> getPostLikes(@PathVariable("postId") Long postId,
                                          @RequestParam("page") Integer page,
                                          @RequestParam("size") Integer size) {
        page = page < 0 ? 0 : page-1;
        size = size <= 0 ? 5 : size;
        Post targetPost = postService.getPostById(postId);
        List<User> postLikerList = ussr.getLikesByPostPaginate(targetPost, page, size);
        return new ResponseEntity<>(postLikerList, HttpStatus.OK);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable("postId") Long postId) {
        postService.likePost(postId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{postId}/unlike")
    public ResponseEntity<?> unlikePost(@PathVariable("postId") Long postId) {
        postService.unlikePost(postId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> getPostComments(@PathVariable("postId") Long postId,
                                             @RequestParam("page") Integer page,
                                             @RequestParam("size") Integer size) {
        page = page < 0 ? 0 : page-1;
        size = size <= 0 ? 5 : size;
        Post targetPost = postService.getPostById(postId);
        List<CommentResponse> postCommentResponseList = cmsr.getPostCommentsPaginate(targetPost, page, size);
        return new ResponseEntity<>(postCommentResponseList, HttpStatus.OK);
    }

    @PostMapping("/{postId}/comments/create")
    public ResponseEntity<?> createPostComment(@PathVariable("postId") Long postId,
                                               @RequestParam(value = "content") String content) {
        Comment savedComment = postService.createPostComment(postId, content);
        CommentResponse commentResponse = CommentResponse.builder()
                .comment(savedComment).build();
        return new ResponseEntity<>(commentResponse, HttpStatus.OK);
    }

    @PostMapping("/{postId}/comments/{commentId}/update")
    public ResponseEntity<?> updatePostComment(@PathVariable("commentId") Long commentId,
                                               @PathVariable("postId") Long postId,
                                               @RequestParam(value = "content") String content) {
        Comment savedComment = postService.updatePostComment(commentId, postId, content);
        return new ResponseEntity<>(savedComment, HttpStatus.OK);
    }

    @PostMapping("/{postId}/comments/{commentId}/delete")
    public ResponseEntity<?> deletePostComment(@PathVariable("commentId") Long commentId,
                                               @PathVariable("postId") Long postId) {
        postService.deletePostComment(commentId, postId);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
