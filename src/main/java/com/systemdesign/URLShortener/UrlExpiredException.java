package com.systemdesign.URLShortener;

public class UrlExpiredException extends RuntimeException{

    public UrlExpiredException(String message){
        super(message);
    }
}
