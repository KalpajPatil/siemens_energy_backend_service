package com.siemens.clock.controller;

import com.siemens.clock.dto.AttendanceRequest;
import com.siemens.clock.dto.AttendanceResponse;
import com.siemens.clock.service.AttendanceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ClockInController {
    private static final Logger log = LoggerFactory.getLogger(ClockInController.class);

    private final AttendanceService attendanceService;

    public ClockInController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/attendance")
    public ResponseEntity<AttendanceResponse> recordAttendance(@Valid @RequestBody AttendanceRequest request) {
        log.info("Received attendance request: {} for employee: {}", request.getEventType(), request.getEmployeeId());

        AttendanceResponse response = attendanceService.processAttendance(request);

        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Check-in Service is healthy");
    }

}
