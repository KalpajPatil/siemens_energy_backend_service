package com.siemens.clock.service;

import com.siemens.clock.event.CheckOutEvent;
import com.siemens.clock.exception.LegacySystemException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class LegacySystemService {

    private static final Logger log = LoggerFactory.getLogger(LegacySystemService.class);
    private final RestTemplate restTemplate;
    private final DlqService dlqService;

    @Value("${app.legacy.api.url:http://localhost:8084/api/legacy/report}")
    private String legacyApiUrl;

    public LegacySystemService(RestTemplate restTemplate, DlqService dlqService) {
        this.restTemplate = restTemplate;
        this.dlqService = dlqService;
    }

    @CircuitBreaker(name = "legacy-api", fallbackMethod = "fallback")
    @Retry(name = "legacy-api", fallbackMethod = "fallback")
    public void reportHoursWorked(CheckOutEvent event) {
        log.info("Reporting hours to legacy system for employee: {}", event.getEmployeeId());

        Map<String, Object> request = Map.of(
                "employeeId", event.getEmployeeId(),
                "hoursWorked", event.getHoursWorked()
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                legacyApiUrl,
                request,
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new LegacySystemException("Legacy API returned: " + response.getStatusCode());
        }

        Map<String, String> body = response.getBody();
        if (body != null && !"SUCCESS".equals(body.get("status"))) {
            throw new LegacySystemException("Legacy API failed: " + body.get("message"));
        }

        log.info("Successfully reported hours for employee: {}", event.getEmployeeId());
    }

    public void fallback(CheckOutEvent event, Exception e) {
        log.error("Fallback triggered for employee: {}, error: {}", event.getEmployeeId(), e.getMessage());
        dlqService.sendToLegacyDlq(event, "Resilience fallback: " + e.getMessage());
    }
}