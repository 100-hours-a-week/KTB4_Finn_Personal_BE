package kr.ktb.finn_week6.domain.place.dto;

import kr.ktb.finn_week6.domain.place.Place;

import java.math.BigDecimal;

public record PostPlaceInfo(

        String providerPlaceId,
        String placeName,
        String roadAddressName,
        BigDecimal latitude,
        BigDecimal longitude
) {
    public static PostPlaceInfo createPostLocationResponse(Place place){
        return new PostPlaceInfo(place.getProviderPlaceId(), place.getPlaceName(), place.getRoadAddressName(), place.getLatitude(), place.getLongitude());
    }
}
