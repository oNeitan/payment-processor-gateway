package br.com.oneitan.model.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProcessorRequestDto (
        String correlationId,
        BigDecimal amount,
        Instant requestedAt
) {
    
    public ProcessorRequestDto(String correlationId, BigDecimal amount) {
        this(correlationId, amount, Instant.now());
    }
}
