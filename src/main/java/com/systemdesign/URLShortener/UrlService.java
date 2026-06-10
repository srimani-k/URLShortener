package com.systemdesign.URLShortener;

import org.springframework.stereotype.Service;

import java.util.Optional;
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
        if(inputurl==null || inputurl.isBlank()){
            throw new InvalidUrlException("URL cannot be empty");
        }

        //before generating shortcode, prevent duplicates !!!!
        Optional<UrlMappingEntity> existingUrl = urlRepository.findByOriginalUrl(inputurl);
        if(existingUrl.isPresent()){
            UrlResponseDTO response = new UrlResponseDTO();
            response.setShortenUrl("http://localhost:8080/" + existingUrl.get().getShortCode());
            return response;
        }

        String generateShortCode = UUID.randomUUID().toString().substring(0,6);
        UrlMappingEntity urlbody = new UrlMappingEntity();
        urlbody.setShortCode(generateShortCode);
        urlbody.setOriginalUrl(inputurl);
         urlRepository.save(urlbody);
         UrlResponseDTO urlResponseDTO = new UrlResponseDTO();
         urlResponseDTO.setShortenUrl("http://localhost:8080/"+urlbody.getShortCode());
         return urlResponseDTO;
    }
    public String getOriginalUrlFromShortenUrl(String shortCode){
         UrlMappingEntity url= urlRepository.findByShortCode(shortCode).orElseThrow(()-> new ShortUrlNotFoundException("Short URL not found"));

         //set clickcount
         url.setClickCount(url.getClickCount()+1);
         urlRepository.save(url);
//         UrlResponseDTO response =  new UrlResponseDTO();
////         response.setShortenUrl(url.getShortCode());
//         response.setOriginalUrl(url.getOriginalUrl());
         return url.getOriginalUrl();

    }

    public UrlStatsResponseDTO getUrlStats(String shortCode){
        UrlMappingEntity urlbody = urlRepository.findByShortCode(shortCode).orElseThrow(()->new ShortUrlNotFoundException("Short code not found"));

        UrlStatsResponseDTO response = new UrlStatsResponseDTO();
        response.setOriginalUrl(urlbody.getOriginalUrl());
        response.setShortUrl("http://localhost:8080/"+urlbody.getShortCode());
        response.setClickCount(urlbody.getClickCount());

        return response;
    }
}
