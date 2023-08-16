package com.picgram.exception;

public class PostNotFoundException extends RuntimeException{
    public PostNotFoundException(){
    }

    public PostNotFoundException(String message) {
        super(message);
    }
}
