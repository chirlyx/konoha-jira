package com.konoha.jira.service;

import com.konoha.jira.dto.UserAnalyticsItem;
import com.konoha.jira.repository.NinjaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HokageService {

    private final NinjaRepository ninjaRepository;

    public HokageService(NinjaRepository ninjaRepository) {
        this.ninjaRepository = ninjaRepository;
    }

    public List<UserAnalyticsItem> analytics() {
        return ninjaRepository.findAllActive().stream()
                .map(u -> UserAnalyticsItem.builder()
                        .username(u.getUsername())
                        .village(u.getVillage())
                        .rank(u.getRank())
                        .experience(u.getExperience())
                        .build())
                .toList();
    }

    @Transactional
    public void deactivateNinja(Long userId) {
        ninjaRepository.findById(userId).ifPresent(user -> {
            user.setActive(false);
            ninjaRepository.save(user);
        });
    }
}

