package com.picgram.service.impl;

import com.picgram.exception.BlankCommentException;
import com.picgram.exception.PostNotFoundException;
import com.picgram.model.Comment;
import com.picgram.model.Post;
import com.picgram.model.User;
import com.picgram.repository.PostRepository;
import com.picgram.response.PostResponse;
import com.picgram.service.CommentService;
import com.picgram.service.PostService;
import com.picgram.service.UserService;
import com.picgram.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository prp;
    @Autowired
    private UserService ussr;
    @Autowired
    private Environment env;
    @Autowired
    private CommentService cmsr;


    @Override
    public Post getPostById(Long postId) {
        return prp.findById(postId).orElseThrow(() -> new PostNotFoundException("Post Not Found With ID"));
    }

    @Override
    public PostResponse getPostResponseById(Long postId) {
        User u = ussr.getAuthenticatedUser();
        Post p = getPostById(postId);
        return new PostResponse(p, p.getLikeList().contains(u));
    }

    @Override
    public List<PostResponse> getPostsByUserPaginate(User author, Integer page, Integer size) {
        return prp.findPostsByCreator(author,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateCreated")))
                .stream().map(this::postToPostResponse).collect(Collectors.toList());
    }

    @Override
    public List<PostResponse> getTimelinePostsPaginate(Integer page, Integer size) {
        User authUser = ussr.getAuthenticatedUser();
        List<User> followingList = authUser.getFollowingUsers();
        followingList.add(authUser);
        List<PostResponse> plist = prp.findPostsByCreatorIn(
                        followingList,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateCreated")))
                .stream().map(this::postToPostResponse).collect(Collectors.toList());

        System.out.println(plist);
        return plist;
    }

    @Override
    public Post createNewPost(String description, MultipartFile postPhoto) {
        User authUser = ussr.getAuthenticatedUser();
        Post newPost = new Post();
        newPost.setDescription(description);
        newPost.setCreator(authUser);
        newPost.setLikeCount(0);
        newPost.setCommentCount(0);
        newPost.setDateCreated(new Date());
        newPost.setDateLastModified(new Date());

        if (postPhoto != null && postPhoto.getSize() > 0) {
            String uploadDir = env.getProperty("upload.user.posts");
            String newPhotoName = FileUtils.nameFile(postPhoto);
            String newPhotoUrl = env.getProperty("upload.user.posts") + File.separator + newPhotoName;
            newPost.setPhoto(newPhotoUrl);
            try {
                FileUtils.saveNewFile(uploadDir, newPhotoName, postPhoto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return prp.save(newPost);
    }

    @Override
    public Post updatePost(Long postId, String content) {
        Post targetPost = getPostById(postId);
        if (content != null) {
            targetPost.setDescription(content);
        }
        targetPost.setDateLastModified(new Date());
        return prp.save(targetPost);
    }

    @Override
    public void deletePost(Long postId) {
        User authUser = ussr.getAuthenticatedUser();
        Post targetPost = getPostById(postId);

        if (targetPost.getCreator().equals(authUser)) {
            prp.deleteById(postId);
            if (targetPost.getPhoto() != null) {
                Path path = Paths.get(targetPost.getPhoto());
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                }
            }
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public void likePost(Long postId) {
        User authUser = ussr.getAuthenticatedUser();
        Post targetPost = getPostById(postId);
        if (!targetPost.getLikeList().contains(authUser)) {
            targetPost.setLikeCount(targetPost.getLikeCount() + 1);
            targetPost.getLikeList().add(authUser);
            prp.save(targetPost);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public void unlikePost(Long postId) {
        User authUser = ussr.getAuthenticatedUser();
        Post targetPost = getPostById(postId);
        if (targetPost.getLikeList().contains(authUser)) {
            targetPost.setLikeCount(targetPost.getLikeCount()-1);
            targetPost.getLikeList().remove(authUser);
            prp.save(targetPost);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public Comment createPostComment(Long postId, String content) {
        if (content == null){ throw new BlankCommentException();}

        User authUser = ussr.getAuthenticatedUser();
        Post targetPost = getPostById(postId);
        Comment savedComment = cmsr.createNewComment(content, targetPost);
        targetPost.setCommentCount(targetPost.getCommentCount()+1);
        prp.save(targetPost);

        return savedComment;
    }

    @Override
    public Comment updatePostComment(Long commentId, Long postId, String content) {
        if (content == null){ throw new BlankCommentException();}

        return cmsr.updateComment(commentId, content);
    }

    @Override
    public void deletePostComment(Long commentId, Long postId) {
        Post targetPost = getPostById(postId);
        cmsr.deleteComment(commentId);
        targetPost.setCommentCount(targetPost.getCommentCount()-1);
        targetPost.setDateLastModified(new Date());
        prp.save(targetPost);
    }

    private PostResponse postToPostResponse(Post p) {
        User u = ussr.getAuthenticatedUser();
        return new PostResponse(p, p.getLikeList().contains(u));
    }
}
