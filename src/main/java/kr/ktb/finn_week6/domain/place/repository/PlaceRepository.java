package kr.ktb.finn_week6.domain.place.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import kr.ktb.finn_week6.domain.place.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static kr.ktb.finn_week6.domain.place.QPlace.place;

@Repository
@RequiredArgsConstructor
public class PlaceRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public Optional<Place> findByProviderPlaceId(String placeId){
        return queryFactory
                .select(place)
                .from(place)
                .where(place.providerPlaceId.eq(placeId))
                .fetch()
                .stream().findAny();
    }

    public Place save(Place place){
        em.persist(place);
        return place;
    }


}
