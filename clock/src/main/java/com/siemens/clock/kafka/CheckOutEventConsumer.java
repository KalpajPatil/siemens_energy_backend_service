package com.siemens.clock.kafka;

import com.siemens.clock.event.CheckOutEvent;
import com.siemens.clock.service.EmailService;
import com.siemens.clock.service.LegacySystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CheckOutEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(CheckOutEventConsumer.class);

    @Autowired
    private LegacySystemService legacySystemService;

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "check-out-completed", groupId = "legacy-system")
    public void handleLegacyReporting(CheckOutEvent event) {
        log.info("Received check-out event for legacy reporting: {}", event.getEmployeeId());
        legacySystemService.reportHoursWorked(event);
    }

    @KafkaListener(topics = "check-out-completed", groupId = "email-service")
    public void handleEmailNotification(CheckOutEvent event) {
        log.info("Received check-out event for email notification: {}", event.getEmployeeId());
        emailService.sendCheckOutEmail(event);
    }
}
