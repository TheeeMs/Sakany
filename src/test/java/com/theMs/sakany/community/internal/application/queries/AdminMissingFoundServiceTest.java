package com.theMs.sakany.community.internal.application.queries;

import com.theMs.sakany.community.internal.domain.Alert;
import com.theMs.sakany.community.internal.domain.AlertCategory;
import com.theMs.sakany.community.internal.domain.AlertReportStatus;
import com.theMs.sakany.community.internal.domain.AlertRepository;
import com.theMs.sakany.community.internal.domain.AlertType;
import com.theMs.sakany.community.internal.infrastructure.persistence.AlertJpaRepository;
import com.theMs.sakany.notifications.internal.application.commands.SendNotificationCommandHandler;
import com.theMs.sakany.notifications.internal.domain.NotificationChannel;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminMissingFoundServiceTest {

    @Mock
    private AlertJpaRepository alertJpaRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private SendNotificationCommandHandler sendNotificationCommandHandler;

    private AdminMissingFoundService service;

    @BeforeEach
    void setUp() {
        service = new AdminMissingFoundService(
                alertJpaRepository,
                alertRepository,
                sendNotificationCommandHandler
        );
    }

    @Test
    void updateStatus_shouldRejectNonMissingFoundAlert() {
        UUID reportId = UUID.randomUUID();
        Alert otherAlert = buildAlert(reportId, AlertType.OTHER);
        when(alertRepository.findById(reportId)).thenReturn(Optional.of(otherAlert));

        assertThrows(NotFoundException.class, () -> service.updateStatus(reportId, AlertReportStatus.MATCHED));

        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void notifyReporter_shouldRejectNonMissingFoundAlert() {
        UUID reportId = UUID.randomUUID();
        Alert otherAlert = buildAlert(reportId, AlertType.SUSPICIOUS_ACTIVITY);
        when(alertRepository.findById(reportId)).thenReturn(Optional.of(otherAlert));

        AdminMissingFoundService.NotifyUserRequest request = new AdminMissingFoundService.NotifyUserRequest(
                "Update",
                "Body",
                NotificationChannel.IN_APP
        );

        assertThrows(NotFoundException.class, () -> service.notifyReporter(reportId, request));

        verify(sendNotificationCommandHandler, never()).handle(any());
    }

    @Test
    void deleteReport_shouldRejectNonMissingFoundAlert() {
        UUID reportId = UUID.randomUUID();
        Alert otherAlert = buildAlert(reportId, AlertType.OTHER);
        when(alertRepository.findById(reportId)).thenReturn(Optional.of(otherAlert));

        assertThrows(NotFoundException.class, () -> service.deleteReport(reportId));

        verify(alertJpaRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void updateStatus_shouldAllowMissingFoundAlert() {
        UUID reportId = UUID.randomUUID();
        Alert missingAlert = buildAlert(reportId, AlertType.MISSING);
        when(alertRepository.findById(reportId)).thenReturn(Optional.of(missingAlert));

        service.updateStatus(reportId, AlertReportStatus.MATCHED);

        assertEquals(AlertReportStatus.MATCHED, missingAlert.getStatus());
        verify(alertRepository).save(missingAlert);
    }

    private Alert buildAlert(UUID id, AlertType type) {
        return Alert.reconstitute(
                id,
                UUID.randomUUID(),
                type,
                AlertCategory.OTHER,
                "Sample title",
                "Sample description",
                "Sample location",
                Instant.now(),
                List.of(),
                false,
                null,
                AlertReportStatus.OPEN,
                "+201000000000"
        );
    }
}
