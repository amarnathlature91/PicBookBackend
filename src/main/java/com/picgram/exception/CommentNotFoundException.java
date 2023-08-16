package com.picgram.exception;

public class CommentNotFoundException extends RuntimeException{

    public CommentNotFoundException() {
    }
    public CommentNotFoundException(String msg){
        super(msg);
    }
}
