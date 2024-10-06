package com.natanius.nasa.controller;

import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import com.natanius.nasa.service.NasaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MyController {

    private final NasaService nasaService;


    @GetMapping("links")
    public List<String> getLinks(@RequestParam String sol) {
        return nasaService.getLinks(sol);
    }

    @GetMapping("largest-image")
    public ResponseEntity<byte[]> getLargestImage(@RequestParam String sol) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_TYPE, "image/jpeg");
        byte[] largestImage = nasaService.getLargestImage(sol);
        headers.add(CONTENT_LENGTH, String.valueOf(largestImage.length));
        return new ResponseEntity<>(largestImage, headers, HttpStatus.OK);

    }

}