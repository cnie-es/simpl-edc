package eu.europa.ec.simpl.contractsign.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.spi.monitor.Monitor;

import java.io.IOException;

/**
 * JAX-RS controller that proxies the contract manager's payment-approval endpoints so the UI
 * can act on {@code PENDING_PAYMENT} agreements through the EDC management API without needing
 * direct access to the contract service.
 *
 * <p>Registered only when {@code contractmanager.extension.enabled=true} (provider side).</p>
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("/v3/payment")
public class PaymentApprovalApiController {

    private final Monitor monitor;
    private final EdcHttpClient httpClient;
    private final String contractManagerUrl;
    private final String contractManagerApiKey;

    public PaymentApprovalApiController(Monitor monitor, EdcHttpClient httpClient,
                                        String contractManagerUrl, String contractManagerApiKey) {
        this.monitor = monitor;
        this.httpClient = httpClient;
        this.contractManagerUrl = contractManagerUrl;
        this.contractManagerApiKey = contractManagerApiKey;
    }

    /**
     * Lists all contract agreements currently awaiting a payment confirmation.
     * Proxies {@code GET {contractManagerUrl}/agreements/pending-payment}.
     */
    @GET
    @Path("/pending")
    public Response listPendingPayments() {
        return proxyGet(contractManagerUrl + "/agreements/pending-payment");
    }

    /**
     * Confirms that payment has been received for a held agreement, releasing the negotiation.
     * Proxies {@code POST {contractManagerUrl}/agreements/{contractAgreementId}/payment/confirm}.
     */
    @POST
    @Path("/{contractAgreementId}/confirm")
    public Response confirmPayment(@PathParam("contractAgreementId") String contractAgreementId) {
        return proxyPost(contractManagerUrl + "/agreements/" + contractAgreementId + "/payment/confirm");
    }

    /**
     * Rejects payment for a held agreement, terminating the negotiation.
     * Proxies {@code POST {contractManagerUrl}/agreements/{contractAgreementId}/payment/reject}.
     */
    @POST
    @Path("/{contractAgreementId}/reject")
    public Response rejectPayment(@PathParam("contractAgreementId") String contractAgreementId) {
        return proxyPost(contractManagerUrl + "/agreements/" + contractAgreementId + "/payment/reject");
    }

    private Response proxyGet(String url) {
        var request = new Request.Builder()
                .url(url)
                .addHeader("x-api-key", contractManagerApiKey)
                .addHeader("accept", MediaType.APPLICATION_JSON)
                .get()
                .build();
        return execute(request);
    }

    private Response proxyPost(String url) {
        var request = new Request.Builder()
                .url(url)
                .addHeader("x-api-key", contractManagerApiKey)
                .addHeader("accept", MediaType.APPLICATION_JSON)
                .post(RequestBody.create(new byte[0]))
                .build();
        return execute(request);
    }

    private Response execute(Request request) {
        try (var response = httpClient.execute(request)) {
            var bodyStr = response.body() != null ? response.body().string() : "";
            return Response.status(response.code()).entity(bodyStr).build();
        } catch (IOException e) {
            monitor.severe("Error proxying payment approval request to contract manager: " + e.getMessage(), e);
            return Response.serverError()
                    .entity("{\"error\":\"Failed to reach contract manager\"}")
                    .build();
        }
    }
}
