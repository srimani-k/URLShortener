package com.systemdesign.URLShortener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final RedisTemplate<String,String> redisTemplate;
    private final ObjectMapper objectMapper;
    public UrlService(UrlRepository urlRepository, RedisTemplate<String,String> redisTemplate, ObjectMapper objectMapper){
        this.urlRepository=urlRepository;
        this.redisTemplate=redisTemplate;
        this.objectMapper=objectMapper;
    }

    //1. Receive URL
    //2. Generate short code
    //3. Save in DB
    //4. Return short URL
    public UrlResponseDTO postShortenUrl(String inputurl){
        log.info("Received request to shorten url: {}",inputurl);
        //before generating shortcode, prevent duplicate original URLS*** !!!!
        Optional<UrlMappingEntity> existingUrl = urlRepository.findByOriginalUrl(inputurl);
        if(existingUrl.isPresent()){
            log.info("URL already exists. Returning existing URL.");
            UrlResponseDTO response = new UrlResponseDTO();
            response.setShortenUrl("http://localhost:8080/" + existingUrl.get().getShortCode());
            return response;
        }

        String generateShortCode = generateShortCodeWithUUID();//88uyg
        log.debug("Generated short code: {}",generateShortCode);
        //before saving, check if generated shortcode is already present in db or not (shortcode duplicate check)
        while(urlRepository.findByShortCode(generateShortCode).isPresent()){
            log.warn("Collision detected for shortcode: {}, Generating a new one",generateShortCode);
             generateShortCode = generateShortCodeWithUUID();
        }
        LocalDateTime timeNow = LocalDateTime.now();

        UrlMappingEntity urlbody = UrlMappingEntity.builder().shortCode(generateShortCode).originalUrl(inputurl).createdAt(timeNow).expiresAt(timeNow.plusDays(30)).build();
//        UrlMappingEntity urlbody = new UrlMappingEntity();
//        urlbody.setShortCode(generateShortCode);
//        urlbody.setOriginalUrl(inputurl);
//        urlbody.setCreatedAt(timeNow);
//        urlbody.setExpiresAt(timeNow.plusDays(30));
        urlRepository.save(urlbody);
        log.info("Short URL saved successfully with ID: {}", urlbody.getId());
        UrlResponseDTO urlResponseDTO = new UrlResponseDTO();
        urlResponseDTO.setShortenUrl("http://localhost:8080/"+urlbody.getShortCode());
        return urlResponseDTO;
    }
    private String generateShortCodeWithUUID(){
        return UUID.randomUUID().toString().substring(0,6);
    }
    private void incrementClickCount(UrlMappingEntity urlbody){
        urlbody.setClickCount(urlbody.getClickCount()+1);
        urlRepository.save(urlbody);
    }
    public String getOriginalUrlFromShortenUrl(String shortCode) {

        LocalDateTime timeNow = LocalDateTime.now();

        // ---------- CACHE HIT ----------
        String json = redisTemplate.opsForValue().get(shortCode);

        if (json != null) {
            log.info("Cache HIT for shortcode: {}", shortCode);

            try {
                CachedUrl cachedUrl = objectMapper.readValue(json, CachedUrl.class);

                // Business validation
                if (timeNow.isAfter(cachedUrl.getExpiresAt())) {
                    throw new UrlExpiredException("URL has expired!!");
                }

                // Update click count in MySQL
                UrlMappingEntity urlBody = urlRepository.findByShortCode(shortCode)
                        .orElseThrow(() ->
                                new ShortUrlNotFoundException("Short URL not found!!"));

                incrementClickCount(urlBody);

                log.info("Click count incremented to {} for short code {}",
                        urlBody.getClickCount(),
                        shortCode);

                return cachedUrl.getOriginalUrl();

            } catch (Exception e) {
                log.warn("Failed to read cached JSON for shortcode {}. Falling back to MySQL.",
                        shortCode, e);
            }
        }

        // ---------- CACHE MISS ----------
        log.info("Cache MISS for shortcode: {}", shortCode);

        UrlMappingEntity url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    log.warn("ShortURL not found: {}", shortCode);
                    return new ShortUrlNotFoundException("Short URL not found");
                });

        log.info("Redirect request received for short code: {}", shortCode);

        if (timeNow.isAfter(url.getExpiresAt())) {
            log.warn("Expired URL accessed for short code: {}", shortCode);
            throw new UrlExpiredException("URL has expired!!");
        }

        log.info("Preparing redirect to original URL.");

        incrementClickCount(url);

        log.info("Click count incremented to {} for short code {}",
                url.getClickCount(),
                shortCode);

        // Store in Redis
        try {
            CachedUrl cachedUrl = new CachedUrl(
                    url.getOriginalUrl(),
                    url.getExpiresAt());

            String cachedJson = objectMapper.writeValueAsString(cachedUrl);

            Duration remainingTime =
                    Duration.between(timeNow, url.getExpiresAt());

            System.out.println(redisTemplate.getValueSerializer().getClass().getName());
            redisTemplate.opsForValue().set(
                    shortCode,
                    cachedJson,
                    remainingTime);

            log.info("Stored shortcode: {} in Redis Cache", shortCode);

        } catch (Exception e) {
            log.warn("Failed to store shortcode {} in Redis.", shortCode, e);
        }

        return url.getOriginalUrl();
    }

    public UrlStatsResponseDTO getUrlStats(String shortCode){
        log.info("Fetching statistics for short code: {}", shortCode);
        UrlMappingEntity urlbody = urlRepository.findByShortCode(shortCode).orElseThrow(()->new ShortUrlNotFoundException("Short code not found"));

        UrlStatsResponseDTO response = new UrlStatsResponseDTO();
        response.setOriginalUrl(urlbody.getOriginalUrl());
        response.setShortUrl("http://localhost:8080/"+urlbody.getShortCode());
        response.setClickCount(urlbody.getClickCount());
        log.info("Statistics returned successfully.");
        return response;
    }
}
