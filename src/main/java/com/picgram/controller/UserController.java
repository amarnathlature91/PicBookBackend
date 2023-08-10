package com.picgram.controller;

import com.picgram.dto.PasswordChangeDto;
import com.picgram.dto.RegisterDto;
import com.picgram.dto.UserDetailsDto;
import com.picgram.model.User;
import com.picgram.service.EmailService;
import com.picgram.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

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

    @GetMapping("/verify/{token}")
    public ResponseEntity<?> verifyEmail(@PathVariable("token") String token) {
        ussr.verifyEmail(token);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/update/profile/{uId}")
    public ResponseEntity<?> updateProfilePhoto(@RequestParam("profilePhoto") MultipartFile profilePhoto,@PathVariable long uId) {
        User updatedUser = ussr.updateProfilePhoto(profilePhoto,uId);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PostMapping("/update/email/{uId}")
    public ResponseEntity<?> updateUserEmail(@RequestParam String email,@PathVariable long uId) {
        return new ResponseEntity<>(ussr.updateEmail(email, uId),HttpStatus.OK);
    }

    @PostMapping("/update/password/{uId}")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordChangeDto pdt,@PathVariable long uId){
        return new ResponseEntity<>( ussr.updatePassword(pdt,uId),HttpStatus.OK);
    }

    @PostMapping("/update/info/{uId}")
    public ResponseEntity<?> updateInfo(@RequestBody UserDetailsDto udt,@PathVariable long uId){
        return new ResponseEntity<>(ussr.updateUserDetails(udt,uId),HttpStatus.OK);
    }

    @PostMapping("/follow/{follower}/{toFollow}")
    public ResponseEntity<?> followUser(@PathVariable long follower,@PathVariable long toFollow){
        ussr.followUser(follower,toFollow);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/unfollow/{unFollower}/{toUnFollow}")
    public ResponseEntity<?> unFollowUser(@PathVariable long unFollower,@PathVariable long toUnFollow){
        ussr.unFollowUser(unFollower,toUnFollow);
        return new ResponseEntity<>(HttpStatus.OK);
    }



}
