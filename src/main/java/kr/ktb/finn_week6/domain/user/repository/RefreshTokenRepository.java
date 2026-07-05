package kr.ktb.finn_week6.domain.user.repository;

import jakarta.persistence.EntityManager;
import kr.ktb.finn_week6.security.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {
    private final EntityManager em;

    public Optional<RefreshToken> findByToken(String token) {
        return em.createQuery("SELECT r FROM RefreshToken r WHERE r.token = :token", RefreshToken.class)
                .setParameter("token", token)
                .getResultStream()
                .findFirst();
    }

    public void deleteByUserId(Long userId) {
        em.createQuery("DELETE FROM RefreshToken r WHERE r.userId = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public void save(RefreshToken refreshToken) {
        em.persist(refreshToken);
    }
    public void delete(RefreshToken refreshToken) {
        em.remove(refreshToken);
    }

}
