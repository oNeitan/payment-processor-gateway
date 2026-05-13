package br.com.oneitan.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import br.com.oneitan.integration.ProcessorGateway;
import br.com.oneitan.model.dto.ProcessorRequestDto;
import br.com.oneitan.repository.PaymentRepository;
import br.com.oneitan.repository.PaymentRepository.PaymentDto;
import br.com.oneitan.model.dto.PaymentRequestDto;
import br.com.oneitan.model.dto.PaymentsSummaryDto;
import br.com.oneitan.model.dto.PaymentsSummaryDto.DefaultSummary;
import br.com.oneitan.model.dto.PaymentsSummaryDto.FallbackSummary;
import br.com.oneitan.model.enums.ServerContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PaymentService {

    private final ProcessorGateway gateway;
    private final PaymentRepository repository;

    public void processPayment(PaymentRequestDto request) {
        var processorRequest = new ProcessorRequestDto(request.correlationId(), request.amount());

        gateway.postPayment(processorRequest);
    }

    public void registerPayment(boolean isFallback, ProcessorRequestDto request) {
        String context = isFallback ? ServerContext.FALLBACK.getContext() : ServerContext.DEFAULT.getContext();

        var amountInCents = convertToLong(request.amount());
        var timestampInSec = request.requestedAt().getEpochSecond();
        repository.registerRequest(context, timestampInSec, new PaymentDto(request.correlationId(), amountInCents));
    }

    public PaymentsSummaryDto getPaymentsSummary(Instant startDate, Instant endDate) {
        if (startDate == null || endDate == null) {
            return getSummaryLifeTime();
        }

        return getSummaryByPeriod(startDate, endDate);
    }

    private PaymentsSummaryDto getSummaryLifeTime() {
        var defaultSummary = repository.getByServerContext(ServerContext.DEFAULT.getContext());
        var fallbackSummary = repository.getByServerContext(ServerContext.FALLBACK.getContext());
        
        return convertToPaymentsSummaryDto(defaultSummary, fallbackSummary);
    }

    private PaymentsSummaryDto getSummaryByPeriod(Instant startDate, Instant endDate) {
        var startSec = startDate.getEpochSecond();
        var endSec = endDate.getEpochSecond();

        var defaultSummary = repository.getByServerContextAndPeriod(ServerContext.DEFAULT.getContext(), startSec, endSec);
        var fallbackSummary = repository.getByServerContextAndPeriod(ServerContext.FALLBACK.getContext(), startSec, endSec);

        return convertToPaymentsSummaryDto(defaultSummary, fallbackSummary);
    }

    private PaymentsSummaryDto convertToPaymentsSummaryDto(List<PaymentDto> defaultPayments, List<PaymentDto> fallbackPayments) {
        var defaultAmount = defaultPayments.stream()
            .mapToLong(PaymentDto::amount)
            .sum();

        var fallbackAmount = fallbackPayments.stream()
            .mapToLong(PaymentDto::amount)
            .sum();

        return new PaymentsSummaryDto(
                new DefaultSummary(defaultPayments.size(), convertToBigDecimal(defaultAmount)),
                new FallbackSummary(fallbackPayments.size(), convertToBigDecimal(fallbackAmount))
        );
    }

    private Long convertToLong(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }

    private BigDecimal convertToBigDecimal(Long amount) {
        return BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(100));
    }
}