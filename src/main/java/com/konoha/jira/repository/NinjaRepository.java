package com.konoha.jira.repository;

import com.konoha.jira.entity.Ninja;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class NinjaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<Ninja> findByUsername(String username) {
        TypedQuery<Ninja> query = entityManager.createQuery(
                "SELECT u FROM Ninja u WHERE u.username = :username", Ninja.class);
        query.setParameter("username", username);
        List<Ninja> result = query.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    public Optional<Ninja> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Ninja.class, id));
    }

    public boolean existsByUsername(String username) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(u) FROM Ninja u WHERE u.username = :username", Long.class)
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
                        "SELECT u FROM Ninja u WHERE u.active = true", Ninja.class)
                .getResultList();
    }
}

