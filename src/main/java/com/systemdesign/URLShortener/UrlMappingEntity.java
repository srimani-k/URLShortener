package com.systemdesign.URLShortener;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="url_mapping_entity")
public class UrlMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalUrl;

    @Column(unique = true)
    private String shortCode;

    @Builder.Default
    private Long clickCount = 0L;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}