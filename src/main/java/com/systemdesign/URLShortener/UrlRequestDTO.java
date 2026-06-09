package com.systemdesign.URLShortener;

public class UrlRequestDTO {

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
