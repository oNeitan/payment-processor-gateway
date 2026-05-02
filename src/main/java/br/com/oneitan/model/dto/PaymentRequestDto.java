package br.com.oneitan.model.dto;

import java.math.BigDecimal;

public record PaymentRequestDto(
        String correlationId,
        BigDecimal amount
) {

}
