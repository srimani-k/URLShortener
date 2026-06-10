package com.systemdesign.URLShortener;

import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<UrlResponseDTO> postShortenUrl(@RequestBody UrlRequestDTO urlRequestDTO){
        UrlResponseDTO response = urlService.postShortenUrl(urlRequestDTO.getUrl());
        return ResponseEntity.status(201).body(response);
    }

    //GET /ab12cd
    //1. Receive shortCode
    //2. Find row using findByShortCode()
    //3. Get originalUrl from entity
    //4. Return originalUrl
    //3XX codes mean redirection. The content moved somewhere else, and the server’s sending you to the new location. These can be temporary or permanent.
    @GetMapping("/{shortenCode}")
    public ResponseEntity<Void> getOriginalUrlFromShortenUrl(@PathVariable String shortenCode){ //void bcoz we are not returning anything(string/JSON)
         String originalUrl =  urlService.getOriginalUrlFromShortenUrl(shortenCode);
         return ResponseEntity.status(302).header("Location",originalUrl).build(); //302-Found.build() instead of body() bcoz we are not returning anything
    }
}
