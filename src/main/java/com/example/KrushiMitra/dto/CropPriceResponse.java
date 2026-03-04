package com.example.KrushiMitra.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class CropPriceResponse {
    private String commodity;
    private String state;
    private String market;
    private Double minPrice;
    private Double maxPrice;
    private Double modalPrice;
    private LocalDate priceDate;
    private List<CropPriceResponse> weeklyTrend;
}