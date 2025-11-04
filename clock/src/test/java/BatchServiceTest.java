import com.siemens.clock.model.WorkShift;
import com.siemens.clock.model.ShiftStatus;
import com.siemens.clock.repository.WorkShiftRepository;
import com.siemens.clock.service.BatchService;
import com.siemens.clock.service.EmailService;
import com.siemens.clock.service.LegacySystemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BatchServiceTest {

    private WorkShiftRepository workShiftRepository;
    private LegacySystemService legacySystemService;
    private EmailService emailService;
    private BatchService batchService;

    @BeforeEach
    void setup() {
        workShiftRepository = mock(WorkShiftRepository.class);
        legacySystemService = mock(LegacySystemService.class);
        emailService = mock(EmailService.class);

        batchService = new BatchService(
                workShiftRepository,
                legacySystemService,
                emailService
        );
    }

    @Test
    void testProcessUnreportedShifts() {

        WorkShift shift = new WorkShift("E001", LocalDateTime.now().minusHours(8));
        shift.setEndTime(LocalDateTime.now());
        shift.setHoursWorked(8.0);
        shift.setStatus(ShiftStatus.COMPLETED);
        shift.setReportedToLegacy(false);
        shift.setEmailSent(false);

        when(workShiftRepository.findAllUnreportedShifts())
                .thenReturn(List.of(shift));

        when(workShiftRepository.findAllUnsentEmailShifts())
                .thenReturn(List.of(shift));


        batchService.processUnreportedShifts();


        verify(legacySystemService, times(1))
                .reportHoursWorked(any());

        verify(emailService, times(1))
                .sendCheckOutEmail(any());
    }
}
