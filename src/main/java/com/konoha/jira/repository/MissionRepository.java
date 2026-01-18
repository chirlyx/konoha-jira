package com.konoha.jira.repository;

import com.konoha.jira.entity.Mission;
import com.konoha.jira.enums.MissionRank;
import com.konoha.jira.enums.MissionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class MissionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String FIND_AVAILABLE_MISSIONS_QUERY = "SELECT m FROM Mission m WHERE m.status = :status AND m.rank IN :ranks";
    private static final String FIND_MISSIONS_BY_NINJA_ID_QUERY = "SELECT m FROM Mission m WHERE m.assignedTo.id = :userId";
    private static final String FIND_PENDING_MISSIONS_QUERY = "SELECT m FROM Mission m WHERE m.status = :status";

    public Mission save(Mission mission) {
        Instant now = Instant.now();
        if (mission.getId() == null) {
            mission.setCreatedAt(now);
            mission.setUpdatedAt(now);
            entityManager.persist(mission);
            return mission;
        }
        mission.setUpdatedAt(now);
        return entityManager.merge(mission);
    }

    public Optional<Mission> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Mission.class, id));
    }

    public Optional<Mission> lockById(Long id) {
        Mission mission = entityManager.find(Mission.class, id, LockModeType.PESSIMISTIC_WRITE);
        return Optional.ofNullable(mission);
    }

    public List<Mission> findAvailable(Set<MissionRank> allowedRanks) {
        TypedQuery<Mission> query = entityManager.createQuery(
                FIND_AVAILABLE_MISSIONS_QUERY,
                Mission.class);
        query.setParameter("status", MissionStatus.AVAILABLE);
        query.setParameter("ranks", allowedRanks);
        return query.getResultList();
    }

    public List<Mission> findMissionsByNinjaId(Long userId) {
        return entityManager.createQuery(
                        FIND_MISSIONS_BY_NINJA_ID_QUERY,
                        Mission.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<Mission> findPendingMissions() {
        return entityManager.createQuery(
                        FIND_PENDING_MISSIONS_QUERY,
                        Mission.class)
                .setParameter("status", MissionStatus.PENDING_REVIEW)
                .getResultList();
    }
}

