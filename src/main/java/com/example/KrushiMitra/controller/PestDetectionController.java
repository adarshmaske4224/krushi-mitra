package com.example.KrushiMitra.controller;

import com.example.KrushiMitra.dto.PestDetectionResponse;


import com.example.KrushiMitra.entity.PestReport;
import com.example.KrushiMitra.repository.UserRepository;
import com.example.KrushiMitra.service.PestDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/pest")
@RequiredArgsConstructor
public class PestDetectionController {

    private final PestDetectionService pestDetectionService;
    private final UserRepository userRepository;

    @PostMapping("/detect")
    public ResponseEntity<PestDetectionResponse> detectPest(
            @RequestParam("image") MultipartFile image,
            @RequestParam("cropType") String cropType) throws Exception {
        return ResponseEntity.ok(pestDetectionService.detectPest(image, cropType));
    }

    @GetMapping("/history")
    public ResponseEntity<List<PestReport>> getHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        return ResponseEntity.ok(pestDetectionService.getUserHistory(userId));
    }

    @GetMapping("/alerts/{district}")
    public ResponseEntity<List<Object[]>> getDistrictAlerts(@PathVariable String district) {
        // Returns top pests in a district
        return ResponseEntity.ok(
                pestDetectionService.getUserHistory(0L) // adjust per requirements
                        .stream().limit(5).map(r -> new Object[]{r.getPestName(), r.getDistrict()}).toList()
        );
    }
}