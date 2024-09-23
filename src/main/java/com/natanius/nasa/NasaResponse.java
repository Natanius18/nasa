package com.natanius.nasa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
public class NasaResponse {
    private List<NasaPhoto> photos;
}
