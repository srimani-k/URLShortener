package com.systemdesign.URLShortener;

public class ShortUrlNotFoundException extends RuntimeException{

    public ShortUrlNotFoundException(String message) {
        super(message);
    }
}
