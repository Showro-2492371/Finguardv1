package org.cts.adm.finguard.RiskAlert.Service;

import org.cts.adm.finguard.RiskAlert.Enum.RiskAlertStatus;
import org.cts.adm.finguard.RiskAlert.Exception.RiskAlertNotFoundException;
import org.cts.adm.finguard.RiskAlert.Model.RiskAlert;
import org.cts.adm.finguard.RiskAlert.Repository.RiskAlertRepository;
import org.cts.adm.finguard.TransactionMonitoring.Model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskAlertServiceTest {

	@Mock
	private RiskAlertRepository riskAlertRepository;

	@InjectMocks
	private RiskAlertService riskAlertService;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(riskAlertService, "escalationThreshold", new BigDecimal("80"));
	}

	@Test
	void evaluateAndCreateAlert_setsNewForLowRiskScore() {
		Transaction transaction = new Transaction();
		transaction.setTransactionId("tx-1");
		transaction.setRiskScore(20);

		when(riskAlertRepository.findByTransactionId("tx-1")).thenReturn(Optional.empty());
		when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));

		RiskAlert alert = riskAlertService.evaluateAndCreateAlert(transaction);

		assertEquals(RiskAlertStatus.NEW, alert.getStatus());
		assertEquals(new BigDecimal("20"), alert.getRiskScore());
	}

	@Test
	void evaluateAndCreateAlert_preservesClosedStatusOnReevaluation() {
		Transaction transaction = new Transaction();
		transaction.setTransactionId("tx-2");
		transaction.setRiskScore(95);

		RiskAlert existing = new RiskAlert();
		existing.setAlertId(10L);
		existing.setTransactionId("tx-2");
		existing.setStatus(RiskAlertStatus.CLOSED);

		when(riskAlertRepository.findByTransactionId("tx-2")).thenReturn(Optional.of(existing));
		when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));

		RiskAlert alert = riskAlertService.evaluateAndCreateAlert(transaction);

		assertEquals(RiskAlertStatus.CLOSED, alert.getStatus());
		assertEquals(new BigDecimal("95"), alert.getRiskScore());
	}

	@Test
	void updateStatus_rejectsInvalidTransition() {
		RiskAlert alert = new RiskAlert();
		alert.setAlertId(25L);
		alert.setStatus(RiskAlertStatus.NEW);

		when(riskAlertRepository.findById(25L)).thenReturn(Optional.of(alert));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> riskAlertService.updateStatus(25L, RiskAlertStatus.RESOLVED));

		assertTrue(ex.getMessage().contains("Invalid status transition"));
		verify(riskAlertRepository, never()).save(any(RiskAlert.class));
	}

	@Test
	void updateStatus_mapsLegacyStatusBeforeValidatingTransition() {
		RiskAlert alert = new RiskAlert();
		alert.setAlertId(26L);
		alert.setStatus(RiskAlertStatus.FLAGGED);

		when(riskAlertRepository.findById(26L)).thenReturn(Optional.of(alert));
		when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));

		RiskAlert updated = riskAlertService.updateStatus(26L, RiskAlertStatus.REVIEWED);

		assertEquals(RiskAlertStatus.REVIEWED, updated.getStatus());
		verify(riskAlertRepository, times(1)).save(alert);
	}

	@Test
	void getAlertById_throwsWhenMissing() {
		when(riskAlertRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(RiskAlertNotFoundException.class, () -> riskAlertService.getAlertById(99L));
	}
}

