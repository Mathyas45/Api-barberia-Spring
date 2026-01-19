package com.barberia.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class TimeZoneConfig {

    @PostConstruct
    public void init() {
        // Establecer la zona horaria de Perú (America/Lima - UTC-5)
        TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
    }
}
