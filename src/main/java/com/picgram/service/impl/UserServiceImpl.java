package com.picgram.service.impl;

import com.picgram.dto.PasswordChangeDto;
import com.picgram.dto.RegisterDto;
import com.picgram.dto.ResetPasswordDto;
import com.picgram.dto.UserDetailsDto;
import com.picgram.exception.EmailAlreadyRegisteredException;
import com.picgram.exception.UserNotFoundException;
import com.picgram.model.EmailVerification;
import com.picgram.model.User;
import com.picgram.repository.EmailServiceRepository;
import com.picgram.repository.UserRepository;
import com.picgram.response.UserResponse;
import com.picgram.service.EmailService;
import com.picgram.service.UserService;
import com.picgram.utils.FileUtils;
import com.picgram.utils.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository urp;
    @Autowired
    private EmailService ems;
    @Autowired
    private EmailServiceRepository erp;
    @Autowired
    private Environment env;

    public User getById(long uId) {
        return urp.findById(uId).orElseThrow(() -> new UserNotFoundException("User Not Found With Given ID"));
    }

    @Override
    public User getByEmail(String email) {
        User u = urp.getByEmail(email);
        if (u != null) {
            return u;
        } else {
            throw new UserNotFoundException("User Not Found with given Email");
        }
    }

    @Override
    public User create(RegisterDto rdt) {
        User savedU = null;
        try {
            User user = getByEmail(rdt.getEmail());
            if (user != null) {
                throw new EmailAlreadyRegisteredException("Email is Already Registered");
            }
        } catch (UserNotFoundException e) {
            User u = new User();
            u.setEmail(rdt.getEmail());
            u.setPassword(rdt.getPassword());
            u.setFirstName(rdt.getFirstName());
            u.setLastName(rdt.getLastName());
            u.setDateLastModified(new Date());
            u.setJoinDate(new Date());
            u.setRole(Role.ROLE_USER.name());
            u.setFollowerCount(0);
            u.setFollowingCount(0);
            u.setEmailVerified(false);
            savedU = urp.save(u);
            ems.sendVerificationEmail(savedU.getEmail());
        }
        return savedU;
    }

    @Override
    public void verifyEmail(String token) {
        try {
            EmailVerification emv = erp.findById(token).orElseThrow(() -> new RuntimeException("Email Not Found"));
            User u = getByEmail(emv.getEmail());
            u.setEmailVerified(true);
            u.setDateLastModified(new Date());
            urp.save(u);
            erp.deleteById(token);
        } catch (Exception e) {

        }
    }

    @Override
    public void deleteUser(long uid) {

    }

    @Override
    public User updateProfilePhoto(MultipartFile photo, long uid) {
        User targetUser = getById(uid);
        if (!photo.isEmpty() && photo.getSize() > 0) {
            String uploadDir = env.getProperty("upload.user.profiles");
            String oldPhotoName = targetUser.getProfilePhoto();
            String newPhotoName = FileUtils.nameFile(photo);
            String newPhotoUrl = env.getProperty("upload.user.profiles") + File.separator + newPhotoName;
            try {
                if (oldPhotoName == null) {
                    FileUtils.saveNewFile(uploadDir, newPhotoName, photo);
                    targetUser.setProfilePhoto(newPhotoUrl);
                } else {
                    FileUtils.updateFile(uploadDir, oldPhotoName, newPhotoName, photo);
                    targetUser.setProfilePhoto(newPhotoUrl);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return urp.save(targetUser);
    }

    @Override
    public User updateEmail(String email, long uId) {
        User u = urp.findById(uId).orElseThrow(() -> new UserNotFoundException("User Not Found With Given ID"));

        if (!email.equalsIgnoreCase(u.getEmail())) {
            try {
                User duplicateUser = getByEmail(email);
                if (duplicateUser != null) {
                    throw new EmailAlreadyRegisteredException("Email Already Registered With Other User");
                }
                else {
                    throw new UserNotFoundException();
                }
            } catch (UserNotFoundException e) {
                u.setEmail(email);
                u.setEmailVerified(false);
                u.setDateLastModified(new Date());
                User updatedUser = urp.save(u);
                ems.sendVerificationEmail(email);
                return updatedUser;
            }
        } else {
            throw new EmailAlreadyRegisteredException("Email is Same as Previous one");
        }
    }

    @Override
    public User updatePassword(PasswordChangeDto pdt,long uId) {
        User u = urp.findById(uId).orElseThrow(() -> new UserNotFoundException("User Not Found With Id"));
        if (u.getPassword().equals(pdt.getOldPassword())){
            u.setPassword(pdt.getNewPassword());
            return urp.save(u);
        }else {
            throw new RuntimeException("Password did not match to your old password");
        }
    }

    @Override
    public User updateUserDetails(UserDetailsDto udt, long uid) {

        User u=urp.findById(uid).orElseThrow(()-> new UserNotFoundException("User Not Found With Given Id"));
        if (udt.getFirstName() != null){
            u.setFirstName(udt.getFirstName());
        }
        if (udt.getLastName()!=null){
            u.setLastName(udt.getLastName());
        }
        if (udt.getBio()!=null){
            u.setBio(udt.getBio());
        }
        if (udt.getAddress()!=null){
            u.setAddress(udt.getAddress());
        }
        if (udt.getBirthDate()!=null){
            u.setBirthDate(udt.getBirthDate());
        }
        return urp.save(u);
    }

    @Override
    public void followUser(long followerId, long followToId) {
        User followerUser = urp.findById(followerId).orElseThrow(()->new RuntimeException("User Not Found"));
            User userToFollow = getById(followToId);
            followerUser.getFollowingUsers().add(userToFollow);
            followerUser.setFollowingCount(followerUser.getFollowingCount() + 1);
            userToFollow.getFollowerUsers().add(followerUser);
            userToFollow.setFollowerCount(userToFollow.getFollowerCount() + 1);
            urp.save(userToFollow);
            urp.save(followerUser);
    }

    @Override
    public void unFollowUser(long unFollowerId, long unFollowToId) {
        User authUser = urp.findById(unFollowerId).orElseThrow(()->new UserNotFoundException("User Not Found With ID"));

            User userToUnfollow = getById(unFollowToId);
            authUser.getFollowingUsers().remove(userToUnfollow);
            authUser.setFollowingCount(authUser.getFollowingCount() - 1);
            userToUnfollow.getFollowerUsers().remove(authUser);
            userToUnfollow.setFollowerCount(userToUnfollow.getFollowerCount() - 1);
            urp.save(userToUnfollow);
            urp.save(authUser);
    }

    @Override
    public void forgotPassword(String email) {
        try {
            User user = getByEmail(email);
            ems.sendPasswordResetEmail(email);
        } catch (UserNotFoundException ignored) {}
    }

    public User resetPassword(String token, ResetPasswordDto rdt){
        EmailVerification emv = erp.findById(token).orElseThrow(() -> new RuntimeException("Something went wrong! please try with another reset link"));
        User u = getByEmail(emv.getEmail());
        u.setPassword(rdt.getPassword());
        return urp.save(u);
    }

    @Override
    public List<UserResponse> getUserSearchResult(String key, Integer page, Integer size,long uId) {
        if (key.length() < 3) throw new RuntimeException();

        return urp.findUsersByName(key,PageRequest.of(page, size))
                .stream().map((user)->userToUserResponse(user,uId)).collect(Collectors.toList());
    }

    private UserResponse userToUserResponse(User user,long Id) {
        User authUser = urp.findById(Id).orElseThrow(()->new UserNotFoundException("User Not Found With Id"));
        return UserResponse.builder()
                .user(user)
                .followedByAuthUser(user.getFollowerUsers().contains(authUser))
                .build();
    }


}
