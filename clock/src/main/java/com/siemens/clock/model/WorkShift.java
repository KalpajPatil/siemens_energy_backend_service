package com.siemens.clock.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "work_shift")
@AllArgsConstructor
@Getter
@Setter
public class WorkShift {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String employeeId;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Double hoursWorked;

    @Enumerated(EnumType.STRING)
    private ShiftStatus status;

    private boolean reportedToLegacy;
    private boolean emailSent;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WorkShift() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public WorkShift(String employeeId, LocalDateTime startTime) {
        this();
        this.employeeId = employeeId;
        this.startTime = startTime;
        this.status = ShiftStatus.ACTIVE;
    }
}
