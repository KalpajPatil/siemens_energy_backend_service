package com.siemens.clock.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceResponse {

    private String status;
    private String message;
    private String workShiftId;

    public AttendanceResponse() {}

    public AttendanceResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public AttendanceResponse(String status, String message, String workShiftId) {
        this(status, message);
        this.workShiftId = workShiftId;
    }

}
