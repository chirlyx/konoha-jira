package com.konoha.jira.bootstrap;

import com.konoha.jira.enums.MissionRank;
import com.konoha.jira.enums.MissionStatus;
import com.konoha.jira.enums.Rank;
import com.konoha.jira.enums.Role;
import com.konoha.jira.entity.Mission;
import com.konoha.jira.repository.MissionRepository;
import com.konoha.jira.entity.Ninja;
import com.konoha.jira.repository.NinjaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private final NinjaRepository ninjaRepository;
    private final MissionRepository missionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(NinjaRepository ninjaRepository,
                      MissionRepository missionRepository,
                      PasswordEncoder passwordEncoder) {
        this.ninjaRepository = ninjaRepository;
        this.missionRepository = missionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedHokage();
        seedMissions();
    }

    private void seedHokage() {
        if (ninjaRepository.existsByUsername("hokage")) {
            return;
        }
        Ninja hokage = Ninja.builder()
                .username("hokage")
                .password(passwordEncoder.encode("admin"))
                .firstName("Minato")
                .lastName("Namikaze")
                .village("Leaf")
                .role(Role.ROLE_HOKAGE)
                .rank(Rank.HOKAGE)
                .experience(10_000)
                .active(true)
                .build();
        ninjaRepository.save(hokage);
    }

    private void seedMissions() {
        List<Mission> existing = missionRepository.findAvailable(
                Set.of(MissionRank.D, MissionRank.C, MissionRank.B, MissionRank.A, MissionRank.S));
        if (!existing.isEmpty()) {
            return;
        }
        missionRepository.save(Mission.builder()
                .title("Доставка свитков")
                .description("Простая доставка документов")
                .rank(MissionRank.D)
                .rewardExperience(MissionRank.D.getDefaultReward())
                .status(MissionStatus.AVAILABLE)
                .build());
        missionRepository.save(Mission.builder()
                .title("Патруль окраины")
                .description("Проверить периметр деревни")
                .rank(MissionRank.C)
                .rewardExperience(MissionRank.C.getDefaultReward())
                .status(MissionStatus.AVAILABLE)
                .build());
    }
}

