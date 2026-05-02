package br.com.oneitan.integration;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import br.com.oneitan.model.dto.ProcessorRequestDto;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

@Path("/payments")
@RequestScoped
@RegisterRestClient(configKey = "payment-processor-default")
public interface PaymentProcessorClient {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void postPayment(ProcessorRequestDto body);
}
