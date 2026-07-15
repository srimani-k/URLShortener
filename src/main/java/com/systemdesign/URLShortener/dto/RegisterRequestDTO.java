package com.systemdesign.URLShortener.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDTO {
    private String email;
    private String username;
    private String password;
}
