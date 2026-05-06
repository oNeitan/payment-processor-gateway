package br.com.oneitan.integration;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import br.com.oneitan.integration.resilience.CircuitBreaker;
import br.com.oneitan.model.dto.ProcessorHealthDto;
import br.com.oneitan.model.enums.CircuitState;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProcessorHealthChecker {

    @Inject
    @RestClient
    private ProcessorDefaultClient client;

    @Inject
    private CircuitBreaker circuitBreaker;
    
    @Scheduled(cron = "*/5 * * * * ?")
    public void checkCircuitBreaker() {
        ProcessorHealthDto healthDto = null;
        CircuitState currentState = circuitBreaker.getCircuitState();
        
        try {
            healthDto = client.healthCheck();
        
        } catch (Exception e) {
            if (currentState == CircuitState.CLOSED 
                || currentState == CircuitState.HALF_OPEN) {
                circuitBreaker.tryOpenCircuit();
            }
            return;
        }
    
        
        if (currentState == CircuitState.CLOSED && healthDto.failling()) {
            circuitBreaker.tryOpenCircuit();
            return;
        }
        
        if (currentState == CircuitState.OPEN && !healthDto.failling()) {
            circuitBreaker.tryHalfOpenCircuit();
            return;
        }
        
        if (currentState == CircuitState.HALF_OPEN && !healthDto.failling()) {
            circuitBreaker.recordHealthSuccess();
            return;
        }
        
        if (currentState == CircuitState.HALF_OPEN && healthDto.failling()) {
            circuitBreaker.recordHealthFailure();
        }
    }   
}
