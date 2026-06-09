package com.systemdesign.URLShortener;

public class UrlResponseDTO {
    private String shortenUrl;

    public UrlResponseDTO(String shortenUrl, String originalUrl) {
        this.shortenUrl = shortenUrl;
    }

    public UrlResponseDTO() {

    }




    public String getShortenUrl() {
        return shortenUrl;
    }

    public void setShortenUrl(String shortenUrl) {
        this.shortenUrl = shortenUrl;
    }
}
