package br.com.oneitan.service;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import br.com.oneitan.integration.PaymentProcessorClient;
import br.com.oneitan.model.dto.ProcessorRequestDto;
import br.com.oneitan.model.dto.PaymentRequestDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentService {

    @Inject
    @RestClient
    private PaymentProcessorClient client;
    
    public void processPayment(PaymentRequestDto request) {
        var processorRequest = new ProcessorRequestDto(request.correlationId(), request.amount());

        client.postPayment(processorRequest);
    }
}
