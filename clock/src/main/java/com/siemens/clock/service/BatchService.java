package com.siemens.clock.service;

import com.siemens.clock.event.CheckOutEvent;
import com.siemens.clock.model.WorkShift;
import com.siemens.clock.repository.WorkShiftRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public BatchService(WorkShiftRepository workShiftRepository,
                        LegacySystemService legacySystemService,
                        EmailService emailService) {
        this.workShiftRepository = workShiftRepository;
        this.legacySystemService = legacySystemService;
        this.emailService = emailService;
    }

    @Scheduled(cron = "${app.batch.processing-time:0 30 22 * * ?}")
    public void processUnreportedShifts() {
        //LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Starting batch processing for date: {}", LocalDate.now());

        // Process all unreported legacy shifts regardless of date
        List<WorkShift> unreportedShifts = workShiftRepository.findAllUnreportedShifts();
        log.info("Found {} unreported shifts for legacy system", unreportedShifts.size());

        for (WorkShift shift : unreportedShifts) {
            legacySystemService.reportHoursWorked(createEventFromShift(shift));
        }

        // Process all unsent emails regardless of date
        List<WorkShift> unsentEmailShifts = workShiftRepository.findAllUnsentEmailShifts();
        log.info("Found {} shifts with unsent emails", unsentEmailShifts.size());

        for (WorkShift shift : unsentEmailShifts) {
            emailService.sendCheckOutEmail(createEventFromShift(shift));
        }

        log.info("Batch processing completed for date: {}", LocalDate.now());
    }

    private CheckOutEvent createEventFromShift(WorkShift shift) {
        return new CheckOutEvent(
                shift.getId(),
                shift.getEmployeeId(),
                shift.getHoursWorked()
        );
    }

}
