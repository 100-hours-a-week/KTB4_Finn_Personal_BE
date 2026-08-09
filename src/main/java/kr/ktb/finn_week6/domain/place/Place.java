package kr.ktb.finn_week6.domain.place;

import jakarta.persistence.*;
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

    @Column(precision = 8, scale = 6)
    BigDecimal latitude;
    @Column(precision = 9, scale = 6)
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
