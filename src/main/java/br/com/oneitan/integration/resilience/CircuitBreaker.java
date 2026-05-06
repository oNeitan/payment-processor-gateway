package br.com.oneitan.integration.resilience;

import java.time.LocalDateTime;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

import br.com.oneitan.model.enums.CircuitState;
import io.quarkus.arc.Lock;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.ServerErrorException;

@ApplicationScoped
public class CircuitBreaker {

    private static CircuitState circuitState = CircuitState.CLOSED;
    private static int failureCount = 0;
    private static int successCount = 0;
    private static final int FAILURE_THRESHOLD = 3;
    private static final int SUCCESS_THRESHOLD = 3;
    private static final long TIMEOUT = 15;
    private static LocalDateTime lastFailureTime;

    public <T> T execute(Supplier<T> action, Supplier<T> fallback) {
        if (circuitState == CircuitState.OPEN) {
            return fallback.get();
        }

        try {
            T result = action.get();
            
            if (circuitState == CircuitState.HALF_OPEN) {
                recordExecutionSuccess();
            }

            return result;
        } catch (ServerErrorException | ProcessingException | CompletionException e) {
            recordExecutionFailure();
            return fallback.get();
        }
    }

    @Lock(Lock.Type.WRITE)
    void recordExecutionSuccess() {
        if (circuitState == CircuitState.HALF_OPEN) {
            successCount++;
            if (successCount >= SUCCESS_THRESHOLD) {
                circuitState = CircuitState.CLOSED;
                successCount = 0;
                failureCount = 0;
            }
        }
    }

    @Lock(Lock.Type.WRITE)
    void recordExecutionFailure() {
        if (circuitState == CircuitState.CLOSED) {
            failureCount++;
            if (failureCount >= FAILURE_THRESHOLD) {
                circuitState = CircuitState.OPEN;
                lastFailureTime = LocalDateTime.now();
                successCount = 0;
                failureCount = 0;
            }
        } else if (circuitState == CircuitState.HALF_OPEN) {
            circuitState = CircuitState.OPEN;
            lastFailureTime = LocalDateTime.now();
            successCount = 0;
            failureCount = 0;
        }
    }

    @Lock(Lock.Type.WRITE)
    public void tryCloseCircuit() {
        if (circuitState == CircuitState.HALF_OPEN) {
            successCount++;
            if (successCount >= SUCCESS_THRESHOLD) {
                circuitState = CircuitState.CLOSED;
                successCount = 0;
                failureCount = 0;
            }
        }
    }

    @Lock(Lock.Type.WRITE)
    public void tryOpenCircuit() {
        if (circuitState == CircuitState.CLOSED) {
            failureCount++;
            if (failureCount >= FAILURE_THRESHOLD) {
                circuitState = CircuitState.OPEN;
                lastFailureTime = LocalDateTime.now();
                successCount = 0;
                failureCount = 0;
            }
        }

        if (circuitState == CircuitState.HALF_OPEN) {
            circuitState = CircuitState.OPEN;
            lastFailureTime = LocalDateTime.now();
            successCount = 0;
            failureCount = 0;
        }
    }

    @Lock(Lock.Type.WRITE)
    public void tryHalfOpenCircuit() {
        if (circuitState == CircuitState.OPEN && lastFailureTime != null) {
            if (LocalDateTime.now().isAfter(lastFailureTime.plusSeconds(TIMEOUT))) {
                circuitState = CircuitState.HALF_OPEN;
                successCount = 0;
                failureCount = 0;
            }
        }
    }

    @Lock(Lock.Type.WRITE)
    public void recordHealthSuccess() {
        if (circuitState == CircuitState.HALF_OPEN) {
            successCount++;
            if (successCount >= SUCCESS_THRESHOLD) {
                circuitState = CircuitState.CLOSED;
                successCount = 0;
                failureCount = 0;
            }
        }
    }

    @Lock(Lock.Type.WRITE)
    public void recordHealthFailure() {
        if (circuitState == CircuitState.HALF_OPEN) {
            circuitState = CircuitState.OPEN;
            lastFailureTime = LocalDateTime.now();
            successCount = 0;
            failureCount = 0;
        }
    }

    @Lock(Lock.Type.READ)
    public CircuitState getCircuitState() {
        return circuitState;
    }
}
