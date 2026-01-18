package com.konoha.jira.repository;

import com.konoha.jira.entity.Ninja;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class NinjaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String FIND_NINJA_BY_USERNAME_QUERY = "SELECT u FROM Ninja u WHERE u.username = :username";
    private static final String EXISTS_BY_USERNAME_QUERY = "SELECT COUNT(u) FROM Ninja u WHERE u.username = :username";
    private static final String FIND_ACTIVE_NINJAS_QUERY = "SELECT u FROM Ninja u WHERE u.active = true";

    public Optional<Ninja> findByUsername(String username) {
        TypedQuery<Ninja> query = entityManager.createQuery(
                FIND_NINJA_BY_USERNAME_QUERY, Ninja.class);
        query.setParameter("username", username);
        List<Ninja> result = query.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    public Optional<Ninja> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Ninja.class, id));
    }

    public boolean existsByUsername(String username) {
        Long count = entityManager.createQuery(
                        EXISTS_BY_USERNAME_QUERY, Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count != null && count > 0;
    }

    public Ninja save(Ninja ninja) {
        Instant now = Instant.now();
        if (ninja.getId() == null) {
            ninja.setCreatedAt(now);
            ninja.setUpdatedAt(now);
            entityManager.persist(ninja);
            return ninja;
        }
        ninja.setUpdatedAt(now);
        return entityManager.merge(ninja);
    }

    public List<Ninja> findAllActive() {
        return entityManager.createQuery(
                        FIND_ACTIVE_NINJAS_QUERY, Ninja.class)
                .getResultList();
    }
}

