package com.siemens.clock.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;


import static com.siemens.clock.service.DlqService.DlqMessage;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Creates a structured alert and simulates raising an incident in an external system.
     * @param dlqMessage The final DLQ message containing all failure context.
     */
    public void raiseIncidentAlert(DlqMessage dlqMessage) {


        IncidentAlert alert = new IncidentAlert(
                "CRITICAL",
                "Unrecoverable Failure in " + dlqMessage.failureSource,
                String.format("Event ID %s for Employee %s failed all retries. Error: %s",
                        dlqMessage.originalEvent.getWorkShiftId(),
                        dlqMessage.originalEvent.getEmployeeId(),
                        dlqMessage.errorMessage),
                Map.of(
                        "employeeId", String.valueOf(dlqMessage.originalEvent.getEmployeeId()),
                        "workShiftId", String.valueOf(dlqMessage.originalEvent.getWorkShiftId()),
                        "source", dlqMessage.failureSource,
                        "dlqTimestamp", String.valueOf(dlqMessage.timestamp)
                )
        );

        simulateExternalCall(alert);
    }

    private void simulateExternalCall(IncidentAlert alert) {
        log.error("--- INCIDENT ALERT RAISED ({}) ---", alert.severity);
        log.error("Title: {}", alert.title);
        log.error("Details: {}", alert.description);
        log.error("Timestamp: {}", alert.timestamp.format(FORMATTER));
        log.error("Metadata: {}", alert.metadata);
        log.error("---------------------------------");

        log.info("Alert successfully forwarded to Incident Management System (Simulated).");
    }

    private static class IncidentAlert {

        public final String severity;
        public final String title;
        public final String description;
        public final Map<String, String> metadata;
        public final LocalDateTime timestamp;

        public IncidentAlert(String severity, String title, String description, Map<String, String> metadata) {
            this.severity = severity;
            this.title = title;
            this.description = description;
            this.metadata = metadata;
            this.timestamp = LocalDateTime.now();
        }
    }
}