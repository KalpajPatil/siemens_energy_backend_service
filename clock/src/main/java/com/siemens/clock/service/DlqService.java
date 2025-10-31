package com.siemens.clock.service;

import com.siemens.clock.event.CheckOutEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class DlqService {

    private static final Logger log = LoggerFactory.getLogger(DlqService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DlqService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendToLegacyDlq(CheckOutEvent event, String errorMessage) {
        log.warn("Sending to Legacy DLQ - Employee: {}, Error: {}", event.getEmployeeId(), errorMessage);

        DlqMessage dlqMessage = new DlqMessage(event, errorMessage, "LEGACY_SYSTEM");
        kafkaTemplate.send("check-out-dlq", dlqMessage);
    }

    public void sendToEmailDlq(CheckOutEvent event, String errorMessage) {
        log.warn("Sending to Email DLQ - Employee: {}, Error: {}", event.getEmployeeId(), errorMessage);

        DlqMessage dlqMessage = new DlqMessage(event, errorMessage, "EMAIL_SERVICE");
        kafkaTemplate.send("email-dlq", dlqMessage);
    }

    private static class DlqMessage {
        public final CheckOutEvent originalEvent;
        public final String errorMessage;
        public final String failureSource;
        public final long timestamp;

        public DlqMessage(CheckOutEvent originalEvent, String errorMessage, String failureSource) {
            this.originalEvent = originalEvent;
            this.errorMessage = errorMessage;
            this.failureSource = failureSource;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
