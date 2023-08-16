package com.picgram.service.impl;

import com.picgram.dto.*;
import com.picgram.exception.EmailAlreadyRegisteredException;
import com.picgram.exception.UserNotFoundException;
import com.picgram.model.EmailVerification;
import com.picgram.model.Post;
import com.picgram.model.User;
import com.picgram.repository.EmailServiceRepository;
import com.picgram.repository.PostRepository;
import com.picgram.repository.UserRepository;
import com.picgram.response.UserResponse;
import com.picgram.security.CustomUserDetailsService;
import com.picgram.security.JwtHelper;
import com.picgram.service.EmailService;
import com.picgram.service.UserService;
import com.picgram.utils.FileUtils;
import com.picgram.utils.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository urp;
    @Autowired
    private EmailService ems;
    @Autowired
    private EmailServiceRepository erp;
    @Autowired
    private Environment env;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private JwtHelper jwtHelper;
    @Autowired
    private AuthenticationManager manager;
    @Autowired
    private PostRepository prp;

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

    public String login(LoginDto ld) {
        try {
            manager.authenticate(new UsernamePasswordAuthenticationToken(ld.getEmail(), ld.getPassword()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Bad Credentials");
        }
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(ld.getEmail());
        return jwtHelper.generateToken(userDetails);
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
    public List<User> getLikesByPostPaginate(Post post, Integer page, Integer size) {
        return urp.findUsersByLikedPosts(
                post,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "firstName", "lastName"))
        );
    }

    @Override
    public User updateProfilePhoto(MultipartFile photo) {
        User targetUser = getAuthenticatedUser();
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
    public User updateEmail(String email) {
        User u = getAuthenticatedUser();

        if (!email.equalsIgnoreCase(u.getEmail())) {
            try {
                User duplicateUser = getByEmail(email);
                if (duplicateUser != null) {
                    throw new EmailAlreadyRegisteredException("Email Already Registered With Other User");
                } else {
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
    public User updatePassword(PasswordChangeDto pdt) {
        User u = getAuthenticatedUser();
//        User u = urp.findById(uId).orElseThrow(() -> new UserNotFoundException("User Not Found With Id"));
        if (u.getPassword().equals(pdt.getOldPassword())) {
            u.setPassword(pdt.getNewPassword());
            return urp.save(u);
        } else {
            throw new RuntimeException("Password did not match to your old password");
        }
    }

    @Override
    public User updateUserDetails(UserDetailsDto udt) {

        User u = getAuthenticatedUser();
        if (udt.getFirstName() != null && udt.getFirstName().length() > 3) {
            u.setFirstName(udt.getFirstName());
        }
        if (udt.getLastName() != null && udt.getLastName().length() > 3) {
            u.setLastName(udt.getLastName());
        }
        if (udt.getBio() != null && udt.getBio().length() > 3) {
            u.setBio(udt.getBio());
        }
        if (udt.getAddress() != null && udt.getAddress().length() > 3) {
            u.setAddress(udt.getAddress());
        }
        if (udt.getBirthDate() != null) {
            u.setBirthDate(udt.getBirthDate());
        }
        return urp.save(u);
    }

    @Override
    public void followUser(long followToId) {
        User followerUser = getAuthenticatedUser();
        User userToFollow = getById(followToId);
        followerUser.getFollowingUsers().add(userToFollow);
        followerUser.setFollowingCount(followerUser.getFollowingCount() + 1);
        userToFollow.getFollowerUsers().add(followerUser);
        userToFollow.setFollowerCount(userToFollow.getFollowerCount() + 1);
        urp.save(userToFollow);
        urp.save(followerUser);
    }

    @Override
    public void unFollowUser(long unFollowToId) {
        User authUser = getAuthenticatedUser();

        User userToUnfollow = getById(unFollowToId);
        authUser.getFollowingUsers().remove(userToUnfollow);
        authUser.setFollowingCount(authUser.getFollowingCount() - 1);
        userToUnfollow.getFollowerUsers().remove(authUser);
        userToUnfollow.setFollowerCount(userToUnfollow.getFollowerCount() - 1);
        urp.save(userToUnfollow);
        urp.save(authUser);
    }

    @Override
    public List<UserResponse> getFollowerUsersPaginate(Long userId, Integer page, Integer size) {
        User u = getById(userId);
        return urp.findUsersByFollowingUsers(u,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "firstName", "lastName")))
                .stream().map(this::userToUserResponse).collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getFollowingUsersPaginate(Long userId, Integer page, Integer size) {
        User targetUser = getById(userId);
        return urp.findUsersByFollowerUsers(targetUser,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "firstName", "lastName")))
                .stream().map(this::userToUserResponse).collect(Collectors.toList());
    }

    @Override
    public void forgotPassword(String email) {
        try {
            User user = getByEmail(email);
            ems.sendPasswordResetEmail(email);
        } catch (UserNotFoundException ignored) {
        }
    }

    public User resetPassword(String token, ResetPasswordDto rdt) {
        EmailVerification emv = erp.findById(token).orElseThrow(() -> new RuntimeException("Something went wrong! please try with another reset link"));
        User u = getByEmail(emv.getEmail());
        u.setPassword(rdt.getPassword());
        return urp.save(u);
    }

    @Override
    public List<UserResponse> getUserSearchResult(String key, Integer page, Integer size) {
        if (key.length() < 3) throw new RuntimeException();

        return urp.findUsersByName(key, PageRequest.of(page, size))
                .stream().map((user) -> userToUserResponse(user)).collect(Collectors.toList());
    }

    private UserResponse userToUserResponse(User user) {
        User authUser = getAuthenticatedUser();
        return UserResponse.builder()
                .user(user)
                .followedByAuthUser(user.getFollowerUsers().contains(authUser))
                .build();
    }

    public final User getAuthenticatedUser() {
        String authUserEmail = SecurityContextHolder.getContext().getAuthentication().getName().toString();
        return getByEmail(authUserEmail);
    }


}
