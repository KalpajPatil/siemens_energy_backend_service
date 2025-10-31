package com.siemens.clock.service;

import com.siemens.clock.dto.AttendanceRequest;
import com.siemens.clock.dto.AttendanceResponse;
import com.siemens.clock.event.CheckOutEvent;
import com.siemens.clock.exception.AttendanceException;
import com.siemens.clock.model.ShiftStatus;
import com.siemens.clock.model.WorkShift;
import com.siemens.clock.repository.WorkShiftRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceService.class);
    private final WorkShiftRepository workShiftRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AttendanceService(WorkShiftRepository workShiftRepository,
                             KafkaTemplate<String, Object> kafkaTemplate) {
        this.workShiftRepository = workShiftRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public AttendanceResponse processAttendance(AttendanceRequest request) {
        try {
            return switch (request.getEventType()) {
                case CHECK_IN -> handleCheckIn(request);
                case CHECK_OUT -> handleCheckOut(request);
                default -> throw new AttendanceException("Invalid event type: " + request.getEventType());
            };
        } catch (AttendanceException e) {
            return new AttendanceResponse("ERROR", e.getMessage());
        } catch (Exception e) {
            return new AttendanceResponse("ERROR", "System error occurred");
        }
    }

    private AttendanceResponse handleCheckIn(AttendanceRequest request) {
        // Check if employee already has active shift
        Optional<WorkShift> activeShift = workShiftRepository.findByEmployeeIdAndStatus(
                request.getEmployeeId(), ShiftStatus.ACTIVE);

        if (activeShift.isPresent()) {
            throw new AttendanceException("Employee " + request.getEmployeeId() + " is already checked in");
        }

        // Create new work shift
        WorkShift workShift = new WorkShift(request.getEmployeeId(), LocalDateTime.now());
        WorkShift saved = workShiftRepository.save(workShift);

        return new AttendanceResponse("SUCCESS", "Checked in successfully", saved.getId().toString());
    }

    private AttendanceResponse handleCheckOut(AttendanceRequest request) {
        // Find active shift
        Optional<WorkShift> activeShift = workShiftRepository.findByEmployeeIdAndStatus(
                request.getEmployeeId(), ShiftStatus.ACTIVE);
        log.info("inside handleCheckOut");
        if (activeShift.isEmpty()) {
            throw new AttendanceException("Employee " + request.getEmployeeId() + " is not checked in");
        }

        WorkShift shift = activeShift.get();
        LocalDateTime endTime = LocalDateTime.now();

        // Calculate hours worked
        Duration duration = Duration.between(shift.getStartTime(), endTime);
        double hoursWorked = duration.toMinutes() / 60.0;
        log.info("hours worked = {}",hoursWorked);
        // Update shift
        shift.setEndTime(endTime);
        shift.setHoursWorked(hoursWorked);
        shift.setStatus(ShiftStatus.COMPLETED);
        WorkShift updated = workShiftRepository.save(shift);

        // Publish check-out event
        CheckOutEvent event = new CheckOutEvent(
                updated.getId().toString(),
                updated.getEmployeeId(),
                updated.getHoursWorked()
        );
        log.info("check-out event = {}", event.getEventId());

        kafkaTemplate.send("check-out-completed", event);

        return new AttendanceResponse("SUCCESS", "Checked out successfully. Hours worked: " + hoursWorked);
    }
}
