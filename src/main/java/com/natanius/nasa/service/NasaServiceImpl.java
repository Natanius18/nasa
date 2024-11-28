package com.natanius.nasa.service;

import static java.util.Comparator.comparingLong;
import static org.springframework.http.HttpHeaders.LOCATION;

import com.natanius.nasa.dto.ImageSizeAndLink;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class NasaServiceImpl implements NasaService {

    private final RestTemplate restTemplate;
    private final LinksHelper linksHelper;

    @Override
    public List<String> getLinks(String sol) {
        return linksHelper.getLinks(sol);
    }

    @Override
    @Cacheable("largestImage")
    public byte[] getLargestImage(String sol) {
        return linksHelper.getLinks(sol).parallelStream()
            .map(this::getImageSizeAndLink)
            .max(comparingLong(ImageSizeAndLink::size))
            .map(imageSizeAndLink -> restTemplate.getForObject(imageSizeAndLink.link(), byte[].class))
            .orElseThrow(() -> new ResourceNotFoundException("Failed to find the largest image"));
    }

    private ImageSizeAndLink getImageSizeAndLink(String imageUrl) {
        ResponseEntity<Void> response = restTemplate.exchange(imageUrl, HttpMethod.HEAD, null, Void.class);

        while (response.getStatusCode().is3xxRedirection()) {
            log.info("Redirecting...");
            imageUrl = response.getHeaders().getFirst(LOCATION);
            response = restTemplate.exchange(imageUrl, HttpMethod.HEAD, null, Void.class);
        }

        if (response.getStatusCode().is2xxSuccessful()) {
            return new ImageSizeAndLink(response.getHeaders().getContentLength(), imageUrl);
        }
        throw new ResourceNotFoundException("Failed to fetch image size. Response Code: " + response.getStatusCode());
    }

}
