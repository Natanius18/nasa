package com.natanius.nasa.controller;

import static java.lang.System.currentTimeMillis;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import com.natanius.nasa.service.NasaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NasaController {

    private final NasaService nasaService;


    @GetMapping("links")
    public List<String> getLinks(@RequestParam String sol) {
        return nasaService.getLinks(sol);
    }

    @GetMapping("largest-image")
    public ResponseEntity<byte[]> getLargestImage(@RequestParam String sol) {
        long startTime = currentTimeMillis();
        byte[] largestImage = nasaService.getLargestImage(sol);
        log.info("Execution time for getLargestImage: {} ms", currentTimeMillis() - startTime);

        return ResponseEntity.ok()
            .header(CONTENT_TYPE, "image/jpeg")
            .header(CONTENT_LENGTH, String.valueOf(largestImage.length))
            .body(largestImage);
    }

}
