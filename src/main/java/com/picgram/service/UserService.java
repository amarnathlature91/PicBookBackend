package com.picgram.service;

import com.picgram.dto.PasswordChangeDto;
import com.picgram.dto.RegisterDto;
import com.picgram.dto.ResetPasswordDto;
import com.picgram.dto.UserDetailsDto;
import com.picgram.model.User;
import com.picgram.response.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.List;

@Service
public interface UserService {

    public User create(RegisterDto rdt);
    public User getByEmail(String email);
    void verifyEmail(String token);
    void deleteUser(long uid);
    public User updateProfilePhoto(MultipartFile photo,long uid);
    public User updateEmail(String email,long id);
    public User updatePassword(PasswordChangeDto pdt,long uId);
    public User updateUserDetails(UserDetailsDto udt, long uid);
    public void followUser(long followerId,long followToId);
    public void unFollowUser(long unFollowerId,long unFollowToId);
    public void forgotPassword(String email);
    public User resetPassword(String token, ResetPasswordDto rdt);

    public List<UserResponse> getUserSearchResult(String key, Integer page, Integer size, long uId);


    }
