package kr.ktb.finn_week6.domain.place.service;


import kr.ktb.finn_week6.domain.place.Place;
import kr.ktb.finn_week6.domain.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;

}
