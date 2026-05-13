package br.com.oneitan.model.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentsSummaryDto(
        @JsonProperty("default") DefaultSummary defaultSummary,
        @JsonProperty("fallback") FallbackSummary fallbackSummary
    
) {
    public record DefaultSummary(
            Integer totalRequests,
            BigDecimal totalAmount
    ) {}

    public record FallbackSummary(
            Integer totalRequests,
            BigDecimal totalAmount
    ) {}
}