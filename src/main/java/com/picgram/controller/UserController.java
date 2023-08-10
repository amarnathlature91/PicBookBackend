package com.picgram.controller;

import com.picgram.dto.*;
import com.picgram.model.User;
import com.picgram.response.UserResponse;
import com.picgram.service.EmailService;
import com.picgram.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService ussr;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto rdt) {
        User su = ussr.create(rdt);
        return new ResponseEntity<>(su, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login( @RequestBody LoginDto ldt){
        JwtResponse jwtResponse = new JwtResponse(ussr.login(ldt));
        return new ResponseEntity<>(jwtResponse, HttpStatus.OK);
    }

    @GetMapping("/verify/{token}")
    public ResponseEntity<?> verifyEmail(@PathVariable("token") String token) {
        ussr.verifyEmail(token);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/update/profile")
    public ResponseEntity<?> updateProfilePhoto(@RequestParam("profilePhoto") MultipartFile profilePhoto) {
        User updatedUser = ussr.updateProfilePhoto(profilePhoto);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PostMapping("/update/email")
    public ResponseEntity<?> updateUserEmail(@RequestParam String email) {
        return new ResponseEntity<>(ussr.updateEmail(email),HttpStatus.OK);
    }

    @PostMapping("/update/password")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordChangeDto pdt){
        return new ResponseEntity<>( ussr.updatePassword(pdt),HttpStatus.OK);
    }

    @PostMapping("/update/info")
    public ResponseEntity<?> updateInfo(@RequestBody UserDetailsDto udt){
        return new ResponseEntity<>(ussr.updateUserDetails(udt),HttpStatus.OK);
    }

    @PostMapping("/follow/{toFollow}")
    public ResponseEntity<?> followUser(@PathVariable long toFollow){
        ussr.followUser(toFollow);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/unfollow/{toUnFollow}")
    public ResponseEntity<?> unFollowUser(@PathVariable long toUnFollow){
        ussr.unFollowUser(toUnFollow);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUser(@RequestParam("key") String key,
                                        @RequestParam("page") Integer page,
                                        @RequestParam("size") Integer size) {
        page = page < 0 ? 0 : page-1;
        size = size <= 0 ? 5 : size;
        List<UserResponse> userSearchResult = ussr.getUserSearchResult(key, page, size);
        return new ResponseEntity<>(userSearchResult, HttpStatus.OK);
    }
    @PostMapping("/forgot-password/{email}")
    private ResponseEntity<?> forgotPassword(@PathVariable String email){
        ussr.forgotPassword(email);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/reset-password/{token}")
    private ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto rdt, @PathVariable String token){
        return new ResponseEntity<>(ussr.resetPassword(token,rdt),HttpStatus.OK);
    }

    @GetMapping("{userId}/following")
    public ResponseEntity<?> getUserFollowingUsers(@PathVariable("userId") Long userId,
                                                   @RequestParam("page") Integer page,
                                                   @RequestParam("size") Integer size) {
        page = page < 0 ? 0 : page-1;
        size = size <= 0 ? 5 : size;
        List<UserResponse> followingList = ussr.getFollowingUsersPaginate(userId, page, size);
        return new ResponseEntity<>(followingList, HttpStatus.OK);
    }

    @GetMapping("{userId}/follower")
    public ResponseEntity<?> getUserFollowerUsers(@PathVariable("userId") Long userId,
                                                  @RequestParam("page") Integer page,
                                                  @RequestParam("size") Integer size) {
        page = page < 0 ? 0 : page-1;
        size = size <= 0 ? 5 : size;
        List<UserResponse> followingList = ussr.getFollowerUsersPaginate(userId, page, size);
        return new ResponseEntity<>(followingList, HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable("userId") Long userId) {
        User authUser = ussr.getAuthenticatedUser();
        User targetUser = ussr.getById(userId);
        UserResponse userResponse = UserResponse.builder()
                .user(targetUser)
                .followedByAuthUser(targetUser.getFollowerUsers().contains(authUser))
                .build();
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @PostMapping("/account/delete")
    public ResponseEntity<?> deleteUserAccount() {
        ussr.deleteUserAccount();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
