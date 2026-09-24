package eu.europa.ec.simpl.contractsign.controller;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.junit.annotations.ApiTest;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.web.jersey.testfixtures.RestControllerTestBase;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ApiTest
class PaymentApprovalApiControllerTest extends RestControllerTestBase {

    private static final String CONTRACT_MANAGER_URL = "http://contract-manager";
    private static final String API_KEY = "test-key";
    private static final String AGREEMENT_ID = "123e4567-e89b-12d3-a456-426614174000";

    private final Monitor monitor = mock();
    private final EdcHttpClient httpClient = mock();

    @Override
    protected Object controller() {
        return new PaymentApprovalApiController(monitor, httpClient, CONTRACT_MANAGER_URL, API_KEY);
    }

    @Test
    void listPendingPaymentsShouldProxyResponseFromContractManager() throws IOException {
        var body = "[{\"contractAgreementId\":\"" + AGREEMENT_ID + "\",\"status\":\"PENDING_PAYMENT\"}]";
        when(httpClient.execute(any())).thenReturn(buildResponse(200, body));

        given()
                .baseUri("http://localhost:" + port + "/v3/payment/pending")
                .contentType(JSON)
                .get()
                .then()
                .log().ifValidationFails()
                .statusCode(200);
    }

    @Test
    void listPendingPaymentsShouldReturn503WhenContractManagerUnreachable() throws IOException {
        when(httpClient.execute(any())).thenThrow(new IOException("connection refused"));

        given()
                .baseUri("http://localhost:" + port + "/v3/payment/pending")
                .contentType(JSON)
                .get()
                .then()
                .log().ifValidationFails()
                .statusCode(500);
    }

    @Test
    void confirmPaymentShouldProxyResponseFromContractManager() throws IOException {
        var body = "{\"contractAgreementId\":\"" + AGREEMENT_ID + "\",\"status\":\"CREATED\"}";
        when(httpClient.execute(any())).thenReturn(buildResponse(200, body));

        given()
                .baseUri("http://localhost:" + port + "/v3/payment/" + AGREEMENT_ID + "/confirm")
                .contentType(JSON)
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(200);
    }

    @Test
    void confirmPaymentShouldForwardNotFoundFromContractManager() throws IOException {
        when(httpClient.execute(any())).thenReturn(buildResponse(404, "{\"status\":404}"));

        given()
                .baseUri("http://localhost:" + port + "/v3/payment/" + AGREEMENT_ID + "/confirm")
                .contentType(JSON)
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }

    @Test
    void rejectPaymentShouldProxyResponseFromContractManager() throws IOException {
        var body = "{\"contractAgreementId\":\"" + AGREEMENT_ID + "\",\"status\":\"TERMINATED\"}";
        when(httpClient.execute(any())).thenReturn(buildResponse(200, body));

        given()
                .baseUri("http://localhost:" + port + "/v3/payment/" + AGREEMENT_ID + "/reject")
                .contentType(JSON)
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(200);
    }

    @Test
    void rejectPaymentShouldForwardBadRequestFromContractManager() throws IOException {
        when(httpClient.execute(any())).thenReturn(buildResponse(400, "{\"status\":400}"));

        given()
                .baseUri("http://localhost:" + port + "/v3/payment/" + AGREEMENT_ID + "/reject")
                .contentType(JSON)
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    private Response buildResponse(int code, String bodyJson) {
        return new Response.Builder()
                .request(new Request.Builder().url(CONTRACT_MANAGER_URL).get().build())
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("")
                .body(ResponseBody.create(bodyJson, MediaType.get("application/json")))
                .build();
    }
}
