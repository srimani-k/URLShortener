package com.systemdesign.URLShortener;

import org.springframework.stereotype.Service;

import java.util.UUID;

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
        String generateShortCode = UUID.randomUUID().toString().substring(0,6);
        UrlMappingEntity urlbody = new UrlMappingEntity();
        urlbody.setShortCode(generateShortCode);
        urlbody.setOriginalUrl(inputurl);
         urlRepository.save(urlbody);

         UrlResponseDTO urlResponseDTO = new UrlResponseDTO();
         urlResponseDTO.setShortenUrl("https://localhost:8080/"+urlbody.getShortCode());
         return urlResponseDTO;
    }
    public String getOriginalUrlFromShortenUrl(String shortenCode){
         UrlMappingEntity url= urlRepository.findByShortCode(shortenCode).orElse(null);

//         UrlResponseDTO response =  new UrlResponseDTO();
////         response.setShortenUrl(url.getShortCode());
//         response.setOriginalUrl(url.getOriginalUrl());
         return url.getOriginalUrl();

    }
//    public UrlResponseDTO getShortenUrl(Long id){
//        UrlResponseDTO  url = new UrlResponseDTO();
//        url = urlRepository.findById(id).orElse(null);
//
//        return url;
//    }
}
