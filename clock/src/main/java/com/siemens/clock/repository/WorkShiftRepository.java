package com.siemens.clock.repository;

import com.siemens.clock.model.ShiftStatus;
import com.siemens.clock.model.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkShiftRepository extends JpaRepository<WorkShift, UUID> {

    Optional<WorkShift> findByEmployeeIdAndStatus(String employeeId, ShiftStatus status);

    @Query("SELECT ws FROM WorkShift ws WHERE ws.status = 'COMPLETED' AND ws.reportedToLegacy = false")
    List<WorkShift> findAllUnreportedShifts();

    @Query("SELECT ws FROM WorkShift ws WHERE ws.status = 'COMPLETED' AND ws.emailSent = false")
    List<WorkShift> findAllUnsentEmailShifts();

    @Query("SELECT COUNT(ws) FROM WorkShift ws WHERE ws.status = 'COMPLETED' AND ws.reportedToLegacy = false AND DATE(ws.endTime) = :date")
    long countUnreportedShiftsByDate(@Param("date") LocalDate date);
}
