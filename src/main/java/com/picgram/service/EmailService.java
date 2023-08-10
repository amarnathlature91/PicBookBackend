package com.picgram.service;

import com.picgram.model.EmailVerification;
import com.picgram.repository.EmailServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmailService {
    @Autowired
    JavaMailSender ms ;
    @Autowired
    private EmailServiceRepository emr;

    public void sendVerificationEmail(String toEmail) {
        String token = String.valueOf(UUID.randomUUID());
        String url="http://localhost:8181/api/user/verify/"+token;

        EmailVerification emv=new EmailVerification();
        emv.setToken(token);
        emv.setEmail(toEmail);
        emr.save(emv);

        String body="click on link to verify your account with PicBook: "+url;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("lature7721@gmail.com");
        message.setTo(toEmail);
        message.setSubject("About Verification");
        message.setText(body);
        ms.send(message);
    }

    public void sendPasswordResetEmail(String email) {
        String rtoken = String.valueOf(UUID.randomUUID());
        String url="http://localhost:8181/api/user/password/reset"+rtoken;

        EmailVerification emvr=new EmailVerification();
        emvr.setToken(rtoken);
        emvr.setEmail(email);
        emr.save(emvr);

        String body="click on link to Reset Your Password: "+url;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("lature7721@gmail.com");
        message.setTo(email);
        message.setSubject("About Password Reset");
        message.setText(body);
        ms.send(message);
    }
}
