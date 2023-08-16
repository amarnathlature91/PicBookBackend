package com.picgram.exception;

public class BlankCommentException extends RuntimeException{
    public BlankCommentException(){

    }

    public BlankCommentException(String message) {
        super(message);
    }
}
