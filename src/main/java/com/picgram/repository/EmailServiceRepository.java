package com.picgram.repository;

import com.picgram.model.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailServiceRepository extends JpaRepository<EmailVerification,String> {
}
