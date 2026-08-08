package kr.ktb.finn_week6.domain.place.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PostLocationRequest(
        @NotBlank
        String providerPlaceId,

        @NotBlank
        @Size(max = 100)
        String placeName,

        @Size(max = 255)
        String roadAddressName,

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        BigDecimal latitude,

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        BigDecimal longitude
) {
}
