package kr.ktb.finn_week6.domain.place.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PostLocationRequest(
        @NotBlank(message = "providerPlaceId is required")
        String providerPlaceId,

        @NotBlank(message = "placeName is required")
        @Size(max = 100,message = "placeName must be less than 100 characters")
        String placeName,

        @Size(max = 255, message = "roadAddressName must be less than 255 characters")
        String roadAddressName,

        @NotNull(message = "latitude is required")
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        BigDecimal latitude,

        @NotNull(message = "longitude is required")
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        BigDecimal longitude
) {
}
