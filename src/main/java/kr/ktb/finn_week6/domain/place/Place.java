package kr.ktb.finn_week6.domain.place;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Getter
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String providerPlaceId;
    private String placeName;
    private String roadAddressName;
    BigDecimal latitude;
    BigDecimal longitude;

    public Place(String providerPlaceId, String placeName, String roadAddressName, BigDecimal latitude, BigDecimal longitude) {
        this.providerPlaceId = providerPlaceId;
        this.placeName = placeName;
        this.roadAddressName = roadAddressName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Place() {

    }
}
