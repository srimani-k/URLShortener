package com.systemdesign.URLShortener.exception;

public class ShortUrlNotFoundException extends RuntimeException{

    public ShortUrlNotFoundException(String message) {
        super(message);
    }
}
