package com.siemens.clock.event;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class CheckOutEvent {

    private String eventId;
    private UUID workShiftId;
    private String employeeId;
    private Double hoursWorked;
    private Instant timestamp;
    private String correlationId;

    public CheckOutEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.correlationId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
    }

    public CheckOutEvent(UUID workShiftId, String employeeId, Double hoursWorked) {
        this();
        this.workShiftId = workShiftId;
        this.employeeId = employeeId;
        this.hoursWorked = hoursWorked;
    }

}
