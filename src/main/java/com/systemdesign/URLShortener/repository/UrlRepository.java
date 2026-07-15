package com.systemdesign.URLShortener.repository;

import com.systemdesign.URLShortener.entity.UrlMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<UrlMappingEntity,Long> {

//      UrlMappingEntity findByShortCode(String shortCode);
    //handle the "not found" case.
    Optional<UrlMappingEntity> findByShortCode(String shortCode);
    Optional<UrlMappingEntity> findByOriginalUrl(String originalUrl);

    boolean existsByEmail(String email);
}
