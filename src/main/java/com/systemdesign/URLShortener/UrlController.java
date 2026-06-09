package com.systemdesign.URLShortener;

import org.springframework.web.bind.annotation.*;

@RestController
public class UrlController {
    private final UrlService urlService;
    public UrlController(UrlService urlService){
        this.urlService=urlService;
    }

    //POST /shorten
    //1. Receive URL
    //2. Generate short code
    //3. Save in DB
    //4. Return short URL - responseDTO
    @PostMapping("/shorten")
    public UrlResponseDTO postShortenUrl(@RequestBody UrlRequestDTO urlRequestDTO){
         return  urlService.postShortenUrl(urlRequestDTO.getUrl());

    }

    //GET /ab12cd
    // 1. Receive shortCode
    //2. Find row using findByShortCode()
    //3. Get originalUrl from entity
    //4. Return originalUrl
    @GetMapping("/{shortenCode}")
    public String getOriginalUrlFromShortenUrl(@PathVariable String shortenCode){
         return urlService.getOriginalUrlFromShortenUrl(shortenCode);
    }
//    @GetMapping("/shorten/{id}")
//    public UrlResponseDTO getShortenUrl(@PathVariable Long id){
//        return urlService.getShortenUrl(id);
//    }
}
