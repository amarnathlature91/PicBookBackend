package com.picgram.security;

import com.picgram.exception.UserNotFoundException;
import com.picgram.model.User;
import com.picgram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor

public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository urp;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = urp.getByEmail(email);
        if (user==null) {
            throw new UserNotFoundException("No user exists with this email.");
        } else {
            return new CustomUserDetails(user);
        }
    }


}
