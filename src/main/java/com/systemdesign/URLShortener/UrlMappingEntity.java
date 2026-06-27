package com.systemdesign.URLShortener;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class UrlMappingEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String originalUrl;

    @Column(unique=true)
    private String shortCode;
    private Long clickCount=0L;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}

