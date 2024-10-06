package com.natanius.nasa.service;

import java.util.List;

public interface NasaService {

    List<String> getLinks(String sol);

    byte[] getLargestImage(String sol);
}
