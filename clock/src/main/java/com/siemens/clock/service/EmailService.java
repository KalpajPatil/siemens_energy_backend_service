package com.siemens.clock.service;

import com.siemens.clock.event.CheckOutEvent;
import com.siemens.clock.exception.EmailServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private DlqService dlqService;

    @CircuitBreaker(name = "email-service", fallbackMethod = "fallback")
    @Retry(name = "email-service", fallbackMethod = "fallback")
    public void sendCheckOutEmail(CheckOutEvent event) {
        log.info("Sending email to employee: {}", event.getEmployeeId());

        // Simulate email sending
        simulateEmailSending();

        log.info("Successfully sent email to employee: {}", event.getEmployeeId());
    }

    public void fallback(CheckOutEvent event, Exception e) {
        log.error("Fallback triggered for email to: {}, error: {}", event.getEmployeeId(), e.getMessage());
        dlqService.sendToEmailDlq(event, "Resilience fallback: " + e.getMessage());
    }

    private void simulateEmailSending() {
        // Simulate random failures for testing (10% failure rate)
        if (Math.random() < 0.1) {
            throw new EmailServiceException("Simulated email service failure");
        }

        // Simulate processing time
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EmailServiceException("Interrupted during email sending");
        }
    }
}