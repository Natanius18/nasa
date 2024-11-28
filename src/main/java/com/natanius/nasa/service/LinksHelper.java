package com.natanius.nasa.service;

import com.natanius.nasa.dto.NasaResponse;
import com.natanius.nasa.dto.PhotoLink;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinksHelper {

    private static final String API_KEY = "kRvQEM0PnOau22QRHjzjxXgZHxxlj5O6hhj6t35P";
    private static final String NASA_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos";

    private final RestTemplate restTemplate;


    @Cacheable("links")
    public List<String> getLinks(String sol) {
        NasaResponse nasaResponse = getListOfLinks(sol);
        log.info("{} links found", nasaResponse.getPhotos().size());
        return nasaResponse.getPhotos().stream().map(PhotoLink::getImg_src).toList();
    }


    private NasaResponse getListOfLinks(String sol) {
        URI uri = UriComponentsBuilder.fromHttpUrl(NASA_URL)
            .queryParam("api_key", API_KEY)
            .queryParam("sol", sol)
            .build().toUri();
        return restTemplate.getForObject(uri, NasaResponse.class);
    }
}
