package com.systemdesign.URLShortener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UrlService {

    private final UrlRepository urlRepository;
    public UrlService(UrlRepository urlRepository){
        this.urlRepository=urlRepository;
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
    public String getOriginalUrlFromShortenUrl(String shortCode){
         UrlMappingEntity url= urlRepository.findByShortCode(shortCode).orElseThrow(()-> {
             log.warn("ShortURL not found: {}",shortCode);
             return new ShortUrlNotFoundException("Short URL not found");
         });
        log.info("Redirect request received for short code: {}", shortCode);
         //check expiration
        LocalDateTime timeNow = LocalDateTime.now();
        if(timeNow.isAfter(url.getExpiresAt())){
            log.warn("Expired URL accessed short code: {}",shortCode);
            throw new UrlExpiredException("URL has expired !!");
        }
        log.info("Redirecting to original URL.");
        //set clickcount
         url.setClickCount(url.getClickCount()+1);
         urlRepository.save(url);
        log.info(
                "Click count incremented to {} for short code {}",
                url.getClickCount(),
                shortCode
        );//         UrlResponseDTO response =  new UrlResponseDTO();
////         response.setShortenUrl(url.getShortCode());
//         response.setOriginalUrl(url.getOriginalUrl());
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
