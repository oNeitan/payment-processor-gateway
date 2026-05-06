package br.com.oneitan.model.dto;

public record ProcessorHealthDto(
        boolean failling,
        Long minResponseTime
) {

}
