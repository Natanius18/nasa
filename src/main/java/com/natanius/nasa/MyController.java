package com.natanius.nasa;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Comparator.comparingInt;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final String NASA_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?sol=300&api_key=kRvQEM0PnOau22QRHjzjxXgZHxxlj5O6hhj6t35P";

    @GetMapping("links")
    public List<String> getLinks() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(NASA_URL).toURL().openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                NasaResponse nasaResponse = OBJECT_MAPPER.readValue(response.toString(), NasaResponse.class);

                System.out.println("Number of photos: " + nasaResponse.getPhotos().size());

                return nasaResponse.getPhotos().stream().map(NasaPhoto::getImg_src).toList();
            } else {
                System.out.println("API Call Failed. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    @GetMapping("largest-image")
    public ResponseEntity<byte[]> getLargestImage() {
        try {
            URL url = new URI(NASA_URL).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                NasaResponse nasaResponse = OBJECT_MAPPER.readValue(response.toString(), NasaResponse.class);
                System.out.println("Number of photos: " + nasaResponse.getPhotos().size());
                Optional<String> largestImage = nasaResponse.getPhotos().parallelStream()
                    .map(nasaPhoto -> getImageSize(nasaPhoto.getImg_src()))
                    .max(comparingInt(ImageSize::size))
                    .map(ImageSize::photo);

                if (largestImage.isPresent()) {
                    byte[] imageBytes = fetchImageWithRedirect(largestImage.get());

                    HttpHeaders headers = new HttpHeaders();
                    headers.add(CONTENT_TYPE, "image/jpeg");

                    return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
                } else {
                    throw new RuntimeException("No images found");
                }
            } else {
                System.out.println("API Call Failed. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
    }

    private ImageSize getImageSize(String imageUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(imageUrl).toURL().openConnection();
            connection.setRequestMethod("HEAD");

            int responseCode = connection.getResponseCode();

            switch (responseCode) {
                case HTTP_OK -> {
                    int contentLength = connection.getContentLength();
                    return new ImageSize(contentLength, imageUrl);
                }
                case HTTP_MOVED_PERM -> {
                    String newUrl = connection.getHeaderField("Location");
                    return getImageSize(newUrl);
                }
                default -> throw new RuntimeException("Failed to fetch image size. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ImageSize(0, imageUrl);
        }
    }


    private byte[] fetchImageWithRedirect(String imageUrl) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URI(imageUrl).toURL().openConnection();

        int responseCode = connection.getResponseCode();

        switch (responseCode) {
            case HTTP_MOVED_PERM -> {
                return fetchImageWithRedirect(connection.getHeaderField("Location"));
            }
            case HTTP_OK -> {
                try (InputStream inputStream = connection.getInputStream();
                     ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                    StreamUtils.copy(inputStream, byteArrayOutputStream);
                    return byteArrayOutputStream.toByteArray();
                }
            }
            default -> throw new RuntimeException("Failed to fetch image. Response Code: " + responseCode);
        }
    }

    private record ImageSize(int size, String photo) {

    }
}
