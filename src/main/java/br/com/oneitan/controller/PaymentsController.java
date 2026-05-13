package br.com.oneitan.controller;

import java.time.Instant;
import br.com.oneitan.model.dto.PaymentRequestDto;
import br.com.oneitan.service.PaymentService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

@Path("/")
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PaymentsController {

    private final PaymentService service;

    @POST
    @Path("/payments")
    public Response processPayment(PaymentRequestDto request) {
        validateRequest(request);

        service.processPayment(request); 
        return Response.ok().build();
    }

    @GET
    @Path("/payments-summary")
    public Response getPaymentsSummary(@QueryParam("from") Instant startDate, @QueryParam("to") Instant endDate) {
        var response = service.getPaymentsSummary(startDate, endDate);
        return Response.ok(response).build();
    }

    private void validateRequest(PaymentRequestDto request) {
        if (request.correlationId() == null || request.correlationId().isBlank() || request.correlationId().length() != 36) {
            throw new IllegalArgumentException("Correlation ID is required and must be a valid UUID.");
        }
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }
}
