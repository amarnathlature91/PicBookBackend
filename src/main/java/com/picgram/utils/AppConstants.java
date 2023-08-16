package com.picgram.utils;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;


public class AppConstants {

    public static final String[] PUBLIC_URLS = {
            "/api/user/register/**",
            "/api/user/login",
            "/api/user/verify/**",
            "/api/user/reset-password/**",
            "/api/user/forgot-password/**",
            "/src/uploads/**",
            "/uploads/**"
    };

    public static final String TOKEN_HEADER="Jwt";

}
