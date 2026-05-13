package br.com.oneitan.integration;

import java.time.temporal.ChronoUnit;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import br.com.oneitan.model.dto.ProcessorHealthDto;
import br.com.oneitan.model.dto.ProcessorRequestDto;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/payments")
@RequestScoped
@RegisterRestClient(configKey = "payment-processor-default")
public interface ProcessorDefaultClient {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Retry(maxRetries = 3, delay = 200, jitter = 100, abortOn = ClientWebApplicationException.class)
    public void postPayment(ProcessorRequestDto body);

    @GET
    @Path("/service-health")
    @Produces(MediaType.APPLICATION_JSON)
    @RateLimit(value = 1, window = 5, windowUnit = ChronoUnit.SECONDS)
    public ProcessorHealthDto healthCheck();
}
