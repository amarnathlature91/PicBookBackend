package com.picgram.service;

import com.picgram.dto.*;
import com.picgram.model.Post;
import com.picgram.model.User;
import com.picgram.response.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.List;

public interface UserService {

    public User create(RegisterDto rdt);
    public String login(LoginDto ld);
    public User getByEmail(String email);
    void verifyEmail(String token);
    public User updateProfilePhoto(MultipartFile photo);
    public User updateEmail(String email);
    public User updatePassword(PasswordChangeDto pdt);
    public User updateUserDetails(UserDetailsDto udt);
    public void followUser(long followToId);
    public void unFollowUser(long unFollowToId);
    public void forgotPassword(String email);
    public User resetPassword(String token, ResetPasswordDto rdt);
    public List<UserResponse> getUserSearchResult(String key, Integer page, Integer size);
    public List<UserResponse> getFollowerUsersPaginate(Long userId, Integer page, Integer size);
    public List<UserResponse> getFollowingUsersPaginate(Long userId, Integer page, Integer size);
    public  User getAuthenticatedUser();
    public User getById(long uId);
    public List<User> getLikesByPostPaginate(Post post, Integer page, Integer size);


    }
