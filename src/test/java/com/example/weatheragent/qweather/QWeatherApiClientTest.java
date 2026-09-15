package com.example.weatheragent.qweather;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class QWeatherApiClientTest {

    @Test
    void preservesSecurityRestrictionDetailsFromErrorResponse() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://test.qweatherapi.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        QWeatherApiClient client = new QWeatherApiClient(builder.build(), new ObjectMapper());

        server.expect(requestTo("https://test.qweatherapi.com/geo/v2/city/lookup"
                        + "?location=%E6%9D%AD%E5%B7%9E&number=1&lang=zh"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                        .body("""
                                {"error":{"status":403,
                                "type":"https://dev.qweather.com/docs/resource/error-code/#security-restriction",
                                "title":"Security Restriction",
                                "detail":"Request is denied by security restriction settings."}}
                                """));

        assertThatThrownBy(() -> client.lookupLocation("杭州"))
                .isInstanceOf(QWeatherApiException.class)
                .extracting("code")
                .isEqualTo("QWEATHER_SECURITY_RESTRICTION");
        server.verify();
    }
}
