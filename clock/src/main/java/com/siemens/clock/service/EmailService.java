package com.siemens.clock.service;

import com.siemens.clock.event.CheckOutEvent;
import com.siemens.clock.exception.EmailServiceException;
import com.siemens.clock.model.WorkShift;
import com.siemens.clock.repository.WorkShiftRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
public class EmailService {

    @Autowired
    private WorkShiftRepository workShiftRepository;

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final RestTemplate restTemplate;
    private final DlqService dlqService;

    @Value("${app.email.service.url:http://localhost:8084/api/email/send}")
    private String emailServiceUrl;

    public EmailService(RestTemplate restTemplate, DlqService dlqService) {
        this.restTemplate = restTemplate;
        this.dlqService = dlqService;
    }

    @CircuitBreaker(name = "email-service", fallbackMethod = "fallback")
    @Retry(name = "email-service", fallbackMethod = "fallback")
    public void sendCheckOutEmail(CheckOutEvent event) {
        log.info("Sending email to employee: {}", event.getEmployeeId());

        Map<String, Object> request = Map.of(
                "to", event.getEmployeeId() + "@company.com",
                "subject", "Daily Work Summary - " + LocalDate.now(),
                "body", String.format("You worked %.2f hours today", event.getHoursWorked())
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                emailServiceUrl,
                request,
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new EmailServiceException("Email service returned: " + response.getStatusCode());
        }

        Map<String, String> body = response.getBody();
        if (body != null && !"SENT".equals(body.get("status"))) {
            throw new EmailServiceException("Email service failed: " + body.get("message"));
        }
        Optional<WorkShift> shift = workShiftRepository.findById(event.getWorkShiftId());
        if(shift.isPresent()){
            shift.get().setEmailSent(true);
            workShiftRepository.save(shift.get());
        }
        log.info("Successfully sent email to employee: {}", event.getEmployeeId());
    }

    public void fallback(CheckOutEvent event, Exception e) {
        log.error("Fallback triggered for email to: {}, error: {}", event.getEmployeeId(), e.getMessage());
        dlqService.sendToEmailDlq(event, "Resilience fallback: " + e.getMessage());
    }
}