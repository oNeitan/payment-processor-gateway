package br.com.oneitan.integration;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import br.com.oneitan.integration.resilience.CircuitBreaker;
import br.com.oneitan.model.dto.ProcessorRequestDto;
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

    public void postPayment(ProcessorRequestDto request) {
        circuitBreaker.execute(
            () -> {
                client.postPayment(request);
                return null;
            },
            () -> {
                fallbackClient.postPayment(request);
                return null;
            }
        );
    }
}
