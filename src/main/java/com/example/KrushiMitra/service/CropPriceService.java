package com.example.KrushiMitra.service;

import com.example.KrushiMitra.dto.CropPriceResponse;
import com.example.KrushiMitra.entity.CropPrice;
import com.example.KrushiMitra.repository.CropPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CropPriceService {

    private final CropPriceRepository cropPriceRepository;

    public CropPriceResponse getCurrentPrice(String commodity,
                                             String state,
                                             String market) {
        // Check DB cache first
        List<CropPrice> cached = cropPriceRepository
                .findByCommodityAndStateAndMarketOrderByPriceDateDesc(
                        commodity, state, market);

        double basePrice = getBasePrice(commodity);

        if (!cached.isEmpty()) {
            CropPrice latest = cached.get(0);
            return CropPriceResponse.builder()
                    .commodity(latest.getCommodity())
                    .state(latest.getState())
                    .market(latest.getMarket())
                    .minPrice(latest.getMinPrice())
                    .maxPrice(latest.getMaxPrice())
                    .modalPrice(latest.getModalPrice())
                    .priceDate(latest.getPriceDate())
                    .weeklyTrend(getMockWeeklyTrend(commodity, state, market, basePrice))
                    .build();
        }

        // ✅ Return mock price data
        double minPrice   = basePrice * 0.95;
        double maxPrice   = basePrice * 1.05;
        double modalPrice = basePrice;

        // Save to DB for caching
        CropPrice price = new CropPrice();
        price.setCommodity(commodity);
        price.setState(state);
        price.setMarket(market);
        price.setMinPrice(minPrice);
        price.setMaxPrice(maxPrice);
        price.setModalPrice(modalPrice);
        price.setPriceDate(LocalDate.now());
        cropPriceRepository.save(price);

        return CropPriceResponse.builder()
                .commodity(commodity)
                .state(state)
                .market(market)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .modalPrice(modalPrice)
                .priceDate(LocalDate.now())
                .weeklyTrend(getMockWeeklyTrend(commodity, state, market, basePrice))
                .build();
    }

    private double getBasePrice(String commodity) {
        return switch (commodity.toLowerCase()) {
            case "wheat"     -> 2150.0;
            case "rice"      -> 3200.0;
            case "onion"     -> 1800.0;
            case "tomato"    -> 2500.0;
            case "sugarcane" -> 3500.0;
            case "cotton"    -> 6200.0;
            case "soybean"   -> 4100.0;
            default          -> 2000.0;
        };
    }

    private List<CropPriceResponse> getMockWeeklyTrend(String commodity,
                                                       String state,
                                                       String market,
                                                       double basePrice) {
        List<CropPriceResponse> trend = new ArrayList<>();
        Random random = new Random();

        for (int i = 6; i >= 0; i--) {
            double variation = (random.nextDouble() - 0.5) * 200;
            double modal = Math.round((basePrice + variation) * 100.0) / 100.0;

            trend.add(CropPriceResponse.builder()
                    .commodity(commodity)
                    .state(state)
                    .market(market)
                    .minPrice(modal * 0.95)
                    .maxPrice(modal * 1.05)
                    .modalPrice(modal)
                    .priceDate(LocalDate.now().minusDays(i))
                    .build());
        }
        return trend;
    }
}