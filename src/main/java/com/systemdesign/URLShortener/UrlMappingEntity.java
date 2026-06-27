package com.systemdesign.URLShortener;

import jakarta.persistence.*;

@Entity
public class UrlMappingEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String originalUrl;

    @Column(unique=true)
    private String shortCode;
    private Long clickCount=0L;

    public UrlMappingEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id =  id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public Long getClickCount() {
        return clickCount;
    }

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }
}

