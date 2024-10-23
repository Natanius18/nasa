package com.natanius.nasa.dto;

import java.util.List;
import lombok.Data;

@Data
public class NasaResponse {
    private List<PhotoLink> photos;
}
