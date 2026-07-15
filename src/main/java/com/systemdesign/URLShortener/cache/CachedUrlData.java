package com.systemdesign.URLShortener.cache;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CachedUrlData {
    private String originalUrl;
    private LocalDateTime expiresAt;
}
