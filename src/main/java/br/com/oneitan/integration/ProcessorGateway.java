package br.com.oneitan.integration;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import br.com.oneitan.integration.resilience.CircuitBreaker;
import br.com.oneitan.model.dto.ProcessorRequestDto;
import br.com.oneitan.service.PaymentService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

@RequestScoped
public class ProcessorGateway {

    @Inject
    @RestClient
    private ProcessorDefaultClient client;

    @Inject
    @RestClient
    private ProcessorFallbackClient fallbackClient;

    @Inject
    private CircuitBreaker circuitBreaker;

    @Inject
    private PaymentService service;

    public void postPayment(ProcessorRequestDto request) {
        circuitBreaker.execute(
            () -> {
                client.postPayment(request);
                service.registerPayment(false, request);
                return null;
            },
            () -> {
                fallbackClient.postPayment(request);
                service.registerPayment(true, request);
                return null;
            }
        );
    }
}
