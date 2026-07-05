package com.systemdesign.URLShortener;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CachedUrl {
    private String originalUrl;
    private LocalDateTime expiresAt;
}
