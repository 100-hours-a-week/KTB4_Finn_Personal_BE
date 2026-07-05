package kr.ktb.finn_week6.domain.user.repository;

import jakarta.persistence.EntityManager;
import kr.ktb.finn_week6.domain.user.User;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class UserRepository {
    EntityManager em;
    public UserRepository(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public void save(User user) {
        em.persist(user);
    }

    public Optional<User> findById(Long id){
        try{
            User user = em.createQuery("select u from User u where u.id = :id AND u.isDeleted = false", User.class)
                    .setParameter("id", id).getSingleResult();
            return Optional.ofNullable(user);
        }catch (Exception e){
            return Optional.empty();
        }
    }

    public boolean existsByEmail(String email) {
        return !em.createQuery("select u from User u where u.email = :email", User.class)
                .setParameter("email", email)
                .getResultList().isEmpty();
    }

    public Optional<User> findByEmail(String email){
        try{
            User user = em.createQuery("select u from User u where u.email = :email AND u.isDeleted = false",
                    User.class).setParameter("email", email).getSingleResult();
            return Optional.ofNullable(user);
        }catch (Exception e){
            return Optional.empty();
        }
    }
}
