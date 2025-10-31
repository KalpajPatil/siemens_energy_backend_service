package com.siemens.clock.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class MockExternalService {

    private static final Logger log = LoggerFactory.getLogger(MockExternalService.class);
    private final Random random = new Random();

    // Mock legacy API endpoint
    @PostMapping("/api/legacy/report")
    public ResponseEntity<Map<String, String>> reportToLegacy(@RequestBody Map<String, Object> request) {
        String employeeId = (String) request.get("employeeId");
        Double hoursWorked = (Double) request.get("hoursWorked");

        log.info("MOCK LEGACY: Reporting {} hours for employee {}", hoursWorked, employeeId);

        // Simulate 20% failure rate for legacy system
        if (random.nextInt(100) < 20) {
            log.warn("MOCK LEGACY: Simulating failure for employee {}", employeeId);
            return ResponseEntity.status(503)
                    .body(Map.of("status", "ERROR", "message", "Legacy system temporarily unavailable"));
        }

        // Simulate slow response (2-5 seconds)
        try {
            Thread.sleep(2000 + random.nextInt(3000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Hours recorded successfully",
                "employeeId", employeeId,
                "recordedHours", hoursWorked.toString()
        ));
    }

    // Mock email service endpoint
    @PostMapping("/api/email/send")
    public ResponseEntity<Map<String, String>> sendEmail(@RequestBody Map<String, Object> request) {
        String to = (String) request.get("to");
        String subject = (String) request.get("subject");
        String body = (String) request.get("body");

        log.info("MOCK EMAIL: Sending email to {} - {}", to, subject);

        // Simulate 10% failure rate for email service
        if (random.nextInt(100) < 10) {
            log.warn("MOCK EMAIL: Simulating failure for {}", to);
            return ResponseEntity.status(503)
                    .body(Map.of("status", "FAILED", "message", "Email service temporarily unavailable"));
        }

        // Simulate processing time (0.5-2 seconds)
        try {
            Thread.sleep(500 + random.nextInt(1500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return ResponseEntity.ok(Map.of(
                "status", "SENT",
                "message", "Email delivered successfully",
                "emailId", UUID.randomUUID().toString(),
                "to", to
        ));
    }
}
