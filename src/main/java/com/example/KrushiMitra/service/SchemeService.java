package com.example.KrushiMitra.service;

import com.example.KrushiMitra.dto.SchemeRecommendationResponse;
import com.example.KrushiMitra.entity.Scheme;
import com.example.KrushiMitra.entity.SchemeRecommendation;
import com.example.KrushiMitra.entity.User;
import com.example.KrushiMitra.repository.SchemeRecommendationRepository;
import com.example.KrushiMitra.repository.SchemeRepository;
import com.example.KrushiMitra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchemeService {

    private final SchemeRepository schemeRepository;
    private final SchemeRecommendationRepository recommendationRepository;
    private final UserRepository userRepository;

    public SchemeRecommendationResponse.ListResponse getRecommendations() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Filter eligible schemes
        List<Scheme> eligible = schemeRepository.findByIsActiveTrue()
                .stream()
                .filter(s -> isEligible(s, user))
                .collect(Collectors.toList());

        List<SchemeRecommendationResponse> results = new ArrayList<>();

        for (Scheme scheme : eligible) {
            // ✅ Mock AI reasoning — replace with real API call later
            String reasoning = buildMockReasoning(scheme, user);

            SchemeRecommendation rec = new SchemeRecommendation();
            rec.setUser(user);
            rec.setScheme(scheme);
            rec.setAiReasoning(reasoning);
            recommendationRepository.save(rec);

            results.add(SchemeRecommendationResponse.builder()
                    .schemeId(scheme.getId())
                    .schemeName(scheme.getName())
                    .description(scheme.getDescription())
                    .benefits(scheme.getBenefits())
                    .applicationUrl(scheme.getApplicationUrl())
                    .aiReasoning(reasoning)
                    .build());
        }

        SchemeRecommendationResponse.ListResponse response =
                new SchemeRecommendationResponse.ListResponse();
        response.setRecommendations(results);
        response.setTotalEligible(results.size());
        return response;
    }

    private boolean isEligible(Scheme scheme, User user) {
        if (scheme.getMaxLandSizeAcres() != null
                && user.getLandSizeAcres() != null
                && user.getLandSizeAcres() > scheme.getMaxLandSizeAcres())
            return false;

        if (scheme.getMaxAnnualIncome() != null
                && user.getAnnualIncome() != null
                && user.getAnnualIncome() > scheme.getMaxAnnualIncome())
            return false;

        if (scheme.getEligibleStates() != null
                && !scheme.getEligibleStates().equalsIgnoreCase("ALL")
                && !scheme.getEligibleStates().contains(user.getState()))
            return false;

        if (scheme.getEligibleCategories() != null
                && user.getCategory() != null
                && !scheme.getEligibleCategories().contains(user.getCategory()))
            return false;

        return true;
    }

    private String buildMockReasoning(Scheme scheme, User user) {
        return "You are eligible for " + scheme.getName()
                + " because your land size is " + user.getLandSizeAcres()
                + " acres (limit: " + scheme.getMaxLandSizeAcres() + " acres)"
                + " and your annual income of Rs." + user.getAnnualIncome()
                + " is within the eligible range.";
    }

    public List<Scheme> getAllSchemes() {
        return schemeRepository.findByIsActiveTrue();
    }
}