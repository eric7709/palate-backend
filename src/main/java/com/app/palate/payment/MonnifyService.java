package com.app.palate.payment;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonnifyService {

        @Value("${monnify.api.key}")
        private String apiKey;

        @Value("${monnify.secret.key}")
        private String secretKey;

        @Value("${monnify.contract.code}")
        private String contractCode;

        private final RestTemplate restTemplate;
        private final ObjectMapper objectMapper;
        private String cachedToken;

        public MonnifyInvoiceResponse createOrderInvoice(Long orderId, double amountNaira, String customerName) {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + getAuthToken());
                headers.setContentType(MediaType.APPLICATION_JSON);

                String invoiceReference = "ORDER-" + orderId + "-" + System.currentTimeMillis();
                String expiryDate = java.time.LocalDateTime.now()
                                .plusHours(24)
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                Map<String, Object> body = new HashMap<>();
                body.put("amount", amountNaira);
                body.put("currencyCode", "NGN");
                body.put("contractCode", contractCode);
                body.put("invoiceReference", invoiceReference);
                body.put("description", "Payment for Order #" + orderId);
                body.put("customerName", customerName);
                body.put("customerEmail", "order" + orderId + "@palate.com");
                body.put("expiryDate", expiryDate);

                log.info("Creating Monnify invoice for order {} with amount {}", orderId, amountNaira);
                log.info("Request body: {}", body);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                try {
                        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                                        "https://sandbox.monnify.com/api/v1/invoice/create",
                                        HttpMethod.POST, entity,
                                        new ParameterizedTypeReference<Map<String, Object>>() {
                                        });

                        log.info("Monnify response status: {}", response.getStatusCode());

                        Map<String, Object> responseBody = objectMapper.convertValue(
                                        response.getBody().get("responseBody"),
                                        new TypeReference<Map<String, Object>>() {
                                        });

                        String accountNumber = (String) responseBody.get("accountNumber");
                        String bankName = (String) responseBody.get("bankName");

                        return new MonnifyInvoiceResponse(accountNumber, bankName, amountNaira, orderId,
                                        invoiceReference);

                } catch (Exception e) {
                        log.error("Monnify invoice creation failed: {}", e.getMessage());
                        throw e;
                }
        }

        private String getAuthToken() {
                String credentials = apiKey + ":" + secretKey;
                String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Basic " + encoded);

                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                                "https://sandbox.monnify.com/api/v1/auth/login",
                                HttpMethod.POST, entity,
                                new ParameterizedTypeReference<Map<String, Object>>() {
                                });

                Map<String, Object> responseBody = objectMapper.convertValue(
                                response.getBody().get("responseBody"),
                                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                                });

                cachedToken = (String) responseBody.get("accessToken");
                return cachedToken;
        }
}