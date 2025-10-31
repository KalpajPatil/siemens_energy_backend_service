package com.siemens.clock.service;

import com.siemens.clock.event.CheckOutEvent;
import com.siemens.clock.exception.LegacySystemException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LegacySystemService {

    private static final Logger log = LoggerFactory.getLogger(LegacySystemService.class);

    @Autowired
    private DlqService dlqService;

    @CircuitBreaker(name = "legacy-api", fallbackMethod = "fallback")
    @Retry(name = "legacy-api", fallbackMethod = "fallback")
    public void reportHoursWorked(CheckOutEvent event) {
        log.info("Reporting hours to legacy system for employee: {}", event.getEmployeeId());

        // Simulate API call - in real implementation, use RestTemplate
        simulateLegacyApiCall();

        log.info("Successfully reported hours for employee: {}", event.getEmployeeId());
    }

    public void fallback(CheckOutEvent event, Exception e) {
        log.error("Fallback triggered for employee: {}, error: {}", event.getEmployeeId(), e.getMessage());
        dlqService.sendToLegacyDlq(event, "Resilience fallback: " + e.getMessage());
    }

    private void simulateLegacyApiCall() {
        // Simulate random failures for testing (30% failure rate)
        if (Math.random() < 0.3) {
            throw new LegacySystemException("Simulated legacy API failure");
        }

        // Simulate processing time
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LegacySystemException("Interrupted during API call");
        }
    }
}