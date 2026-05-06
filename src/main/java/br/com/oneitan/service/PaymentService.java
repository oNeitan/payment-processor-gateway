package br.com.oneitan.service;

import br.com.oneitan.integration.ProcessorGateway;
import br.com.oneitan.model.dto.ProcessorRequestDto;
import br.com.oneitan.model.dto.PaymentRequestDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentService {

    @Inject
    private ProcessorGateway gateway;
    
    public void processPayment(PaymentRequestDto request) {
        var processorRequest = new ProcessorRequestDto(request.correlationId(), request.amount());

        gateway.postPayment(processorRequest);
    }
}