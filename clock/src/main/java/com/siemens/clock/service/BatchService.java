package com.siemens.clock.service;

import com.siemens.clock.model.WorkShift;
import com.siemens.clock.repository.WorkShiftRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class BatchService {
    private static final Logger log = LoggerFactory.getLogger(BatchService.class);

    private final WorkShiftRepository workShiftRepository;
    private final LegacySystemService legacySystemService;
    private final EmailService emailService;

    @Value("${app.batch.retry-attempts:3}")
    private int retryAttempts;

    public BatchService(WorkShiftRepository workShiftRepository,
                        LegacySystemService legacySystemService,
                        EmailService emailService) {
        this.workShiftRepository = workShiftRepository;
        this.legacySystemService = legacySystemService;
        this.emailService = emailService;
    }

    @Scheduled(cron = "${app.batch.processing-time:0 30 22 * * ?}")
    public void processUnreportedShifts() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Starting batch processing for date: {}", yesterday);

        // Process unreported legacy shifts
        List<WorkShift> unreportedShifts = workShiftRepository.findUnreportedShiftsByDate(yesterday);
        log.info("Found {} unreported shifts for legacy system", unreportedShifts.size());

        for (WorkShift shift : unreportedShifts) {
            processShiftWithRetry(shift);
        }

        // Process unsent emails
        List<WorkShift> unsentEmailShifts = workShiftRepository.findUnsentEmailShiftsByDate(yesterday);
        log.info("Found {} shifts with unsent emails", unsentEmailShifts.size());

        for (WorkShift shift : unsentEmailShifts) {
            sendEmailWithRetry(shift);
        }

        log.info("Batch processing completed for date: {}", yesterday);
    }

    private void processShiftWithRetry(WorkShift shift) {
        for (int attempt = 1; attempt <= retryAttempts; attempt++) {
            try {
                legacySystemService.reportHoursWorked(createEventFromShift(shift));
                shift.setReportedToLegacy(true);
                workShiftRepository.save(shift);
                log.info("Successfully reported shift {} in batch (attempt {})", shift.getId(), attempt);
                break;
            } catch (Exception e) {
                log.warn("Failed to report shift {} in batch (attempt {}): {}", shift.getId(), attempt, e.getMessage());
                if (attempt == retryAttempts) {
                    log.error("All retry attempts exhausted for shift {}", shift.getId());
                }
            }
        }
    }

    private void sendEmailWithRetry(WorkShift shift) {
        for (int attempt = 1; attempt <= retryAttempts; attempt++) {
            try {
                emailService.sendCheckOutEmail(createEventFromShift(shift));
                shift.setEmailSent(true);
                workShiftRepository.save(shift);
                log.info("Successfully sent email for shift {} in batch (attempt {})", shift.getId(), attempt);
                break;
            } catch (Exception e) {
                log.warn("Failed to send email for shift {} in batch (attempt {}): {}", shift.getId(), attempt, e.getMessage());
                if (attempt == retryAttempts) {
                    log.error("All retry attempts exhausted for email of shift {}", shift.getId());
                }
            }
        }
    }

    private com.siemens.clock.event.CheckOutEvent createEventFromShift(WorkShift shift) {
        return new com.siemens.clock.event.CheckOutEvent(
                shift.getId().toString(),
                shift.getEmployeeId(),
                shift.getHoursWorked()
        );
    }

}
