package com.systemdesign.URLShortener.dto;

import jakarta.validation.constraints.NotBlank;

public class UrlRequestDTO {

    @NotBlank(message="URL cannot be empty")
    private String url;

    public UrlRequestDTO(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
