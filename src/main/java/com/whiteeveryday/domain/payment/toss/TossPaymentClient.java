package com.whiteeveryday.domain.payment.toss;

import com.whiteeveryday.global.exception.BusinessException;
import com.whiteeveryday.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class TossPaymentClient {

    private final RestClient restClient;
    private final String secretKey;

    public TossPaymentClient(
            RestClient.Builder restClientBuilder,
            @Value("${toss.payments.base-url:https://api.tosspayments.com}") String baseUrl,
            @Value("${toss.payments.secret-key:}") String secretKey) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
        this.secretKey = secretKey;
    }

    public TossPaymentConfirmResponse confirm(TossPaymentConfirmRequest request) {
        try {
            return restClient.post()
                    .uri("/v1/payments/confirm")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(TossPaymentConfirmResponse.class);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED);
        }
    }

    private String authorizationHeader() {
        String token = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        return "Basic " + token;
    }
}
