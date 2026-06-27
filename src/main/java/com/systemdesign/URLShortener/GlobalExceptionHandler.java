package com.systemdesign.URLShortener;

import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ResponseEntity<String> handleShortUrlNotFoundException(ShortUrlNotFoundException ex){
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<String> handleInvalidUrlException(InvalidUrlException ex){
        return ResponseEntity.status(400).body(ex.getMessage());
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<String> handleUrlExpiredException(UrlExpiredException ex){
        return ResponseEntity.status(HttpStatus.GONE).body(ex.getMessage()); //410 - GONE
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotFoundException(MethodArgumentNotValidException ex){
        return ResponseEntity.status(400).body(ex.getBindingResult().getFieldError().getDefaultMessage());
    }
}
