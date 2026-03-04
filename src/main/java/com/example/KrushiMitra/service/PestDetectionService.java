package com.example.KrushiMitra.service;

import com.example.KrushiMitra.dto.PestDetectionResponse;
import com.example.KrushiMitra.entity.PestReport;
import com.example.KrushiMitra.entity.User;
import com.example.KrushiMitra.repository.PestReportRepository;
import com.example.KrushiMitra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PestDetectionService {

    private final PestReportRepository pestReportRepository;
    private final UserRepository userRepository;

    public PestDetectionResponse detectPest(MultipartFile imageFile,
                                            String cropType) {
        // Get logged-in user
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Mock AI response — replace with real API call later
        String pestName = getMockPestName(cropType);
        double confidence = 85.0;
        String treatment = getMockTreatment(pestName);

        // Save report to DB
        PestReport report = new PestReport();
        report.setUser(user);
        report.setCropType(cropType);
        report.setPestName(pestName);
        report.setConfidencePercent(confidence);
        report.setTreatmentRecommendation(treatment);
        report.setState(user.getState());
        report.setDistrict(user.getDistrict());

        PestReport saved = pestReportRepository.save(report);

        // Check district alert
        String alert = checkDistrictAlert(user.getDistrict(), pestName);

        return PestDetectionResponse.builder()
                .reportId(saved.getId())
                .pestName(pestName)
                .confidencePercent(confidence)
                .treatmentRecommendation(treatment)
                .districtAlert(alert)
                .build();
    }

    private String getMockPestName(String cropType) {
        return switch (cropType.toLowerCase()) {
            case "wheat"     -> "Aphids";
            case "rice"      -> "Brown Plant Hopper";
            case "sugarcane" -> "Pyrilla";
            case "cotton"    -> "Bollworm";
            case "onion"     -> "Thrips";
            default          -> "Leaf Blight";
        };
    }

    private String getMockTreatment(String pestName) {
        return switch (pestName) {
            case "Aphids" ->
                    "Apply neem-based pesticide (Azadirachtin 0.03%) at 3ml/litre. " +
                            "Spray in early morning. Repeat after 7 days if needed.";
            case "Brown Plant Hopper" ->
                    "Apply Imidacloprid 17.8 SL at 0.3ml/litre of water. " +
                            "Drain water from field before spraying.";
            case "Pyrilla" ->
                    "Release Epipyrops melanoleuca parasite. " +
                            "Spray Malathion 50 EC at 1.5ml/litre if infestation is severe.";
            case "Bollworm" ->
                    "Spray Spinosad 45 SC at 0.3ml/litre. " +
                            "Set up pheromone traps at 5 per acre.";
            case "Thrips" ->
                    "Apply Fipronil 5 SC at 1.5ml/litre. " +
                            "Remove and destroy infected leaves.";
            default ->
                    "Apply Mancozeb 75 WP at 2g/litre. " +
                            "Ensure proper drainage and avoid overhead irrigation.";
        };
    }

    private String checkDistrictAlert(String district, String pestName) {
        List<Object[]> top = pestReportRepository
                .findTopPestsByDistrict(district);
        for (Object[] row : top) {
            if (row[0].equals(pestName) && ((Long) row[1]) >= 3) {
                return "⚠️ Alert: Multiple cases of " + pestName
                        + " reported in " + district
                        + " district. Take preventive action immediately.";
            }
        }
        return null;
    }

    public List<PestReport> getUserHistory(Long userId) {
        return pestReportRepository
                .findByUserIdOrderByReportedAtDesc(userId);
    }
}