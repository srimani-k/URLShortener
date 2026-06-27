package com.systemdesign.URLShortener;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleShortUrlNotFoundException(ShortUrlNotFoundException ex){

        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidUrlException(InvalidUrlException ex){

        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ErrorResponseDTO> handleUrlExpiredException(UrlExpiredException ex){

        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.GONE.value(),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.GONE).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){

        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getBindingResult()
                        .getFieldError()
                        .getDefaultMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(ShortUrlNotFoundException.class)
//    public ResponseEntity<String> handleShortUrlNotFoundException(ShortUrlNotFoundException ex){
//        return ResponseEntity.status(404).body(ex.getMessage());
//    }
//
//    @ExceptionHandler(InvalidUrlException.class)
//    public ErrorResponseDTO handleInvalidUrlException(InvalidUrlException ex){
//        //return ResponseEntity.status(400).body(ex.getMessage());
//        ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(),400,ex.getMessage());
//        return error;
//    }
//
//    @ExceptionHandler(UrlExpiredException.class)
//    public ResponseEntity<String> handleUrlExpiredException(UrlExpiredException ex){
//        return ResponseEntity.status(HttpStatus.GONE).body(ex.getMessage()); //410 - GONE
//    }
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<String> handleMethodArgumentNotFoundException(MethodArgumentNotValidException ex){
//        return ResponseEntity.status(400).body(ex.getBindingResult().getFieldError().getDefaultMessage());
//    }
//}
