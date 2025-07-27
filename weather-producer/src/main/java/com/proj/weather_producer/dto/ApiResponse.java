package com.proj.weather_producer.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {
    private double langtitude;
    private double longitude;
    private HourlyData hourly;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyData{
        private List<String> time;
        @JsonProperty("temperature_2m")
        private List<Double> temperature2m;
        private List<Double> precipitation;


    }
}


