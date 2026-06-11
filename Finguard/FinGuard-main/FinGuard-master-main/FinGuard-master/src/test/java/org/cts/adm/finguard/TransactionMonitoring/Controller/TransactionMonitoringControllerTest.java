package org.cts.adm.finguard.TransactionMonitoring.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cts.adm.finguard.TransactionMonitoring.Dto.FraudCheckResponse;
import org.cts.adm.finguard.TransactionMonitoring.Dto.TransactionRequest;
import org.cts.adm.finguard.TransactionMonitoring.Enum.ChannelType;
import org.cts.adm.finguard.TransactionMonitoring.Enum.TransactionStatus;
import org.cts.adm.finguard.TransactionMonitoring.Enum.TransactionType;
import org.cts.adm.finguard.TransactionMonitoring.Service.TransactionMonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionMonitoringControllerTest {

    @Mock
    private TransactionMonitoringService service;

    @InjectMocks
    private TransactionMonitoringController controller;

    private ObjectMapper objectMapper;
    private TransactionRequest validRequest;
    private FraudCheckResponse successResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        validRequest = new TransactionRequest();
        validRequest.setCustomerId(1L);
        validRequest.setAmount(new BigDecimal("500"));
        validRequest.setChannel(ChannelType.ONLINE_BANKING);
        validRequest.setTransactionType(TransactionType.DEBITED);

        successResponse = new FraudCheckResponse();
        successResponse.setTransactionId("1-test-uuid");
        successResponse.setCustomerId(1L);
        successResponse.setFraudDetected(false);
        successResponse.setRiskScore(0);
        successResponse.setFraudReason("No fraud indicators detected");
        successResponse.setStatus(TransactionStatus.SUCCESS);
        successResponse.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createTransaction_validRequest_returnsFraudCheckResponse() {
        when(service.createTransaction(any(TransactionRequest.class)))
                .thenReturn(successResponse);

        FraudCheckResponse res = controller.createTransaction(validRequest);

        assertNotNull(res);
        assertEquals("1-test-uuid", res.getTransactionId());
        assertEquals(1L, res.getCustomerId());
        assertFalse(res.isFraudDetected());
        assertEquals(0, res.getRiskScore());
        assertEquals(TransactionStatus.SUCCESS, res.getStatus());
    }

    @Test
    void createTransaction_flaggedTransaction_returnsFlaggedStatus() {
        FraudCheckResponse flaggedResponse = new FraudCheckResponse();
        flaggedResponse.setTransactionId("1-test-uuid-2");
        flaggedResponse.setCustomerId(1L);
        flaggedResponse.setFraudDetected(true);
        flaggedResponse.setRiskScore(40);
        flaggedResponse.setFraudReason("High-value transaction above 50000");
        flaggedResponse.setStatus(TransactionStatus.FLAGGED);
        flaggedResponse.setCreatedAt(LocalDateTime.now());

        when(service.createTransaction(any(TransactionRequest.class)))
                .thenReturn(flaggedResponse);

        FraudCheckResponse res = controller.createTransaction(validRequest);

        assertNotNull(res);
        assertTrue(res.isFraudDetected());
        assertEquals(40, res.getRiskScore());
        assertEquals(TransactionStatus.FLAGGED, res.getStatus());
    }

    @Test
    void createTransaction_blockedTransaction_returnsBlockedStatus() {
        FraudCheckResponse blockedResponse = new FraudCheckResponse();
        blockedResponse.setTransactionId("1-test-uuid-3");
        blockedResponse.setCustomerId(1L);
        blockedResponse.setFraudDetected(true);
        blockedResponse.setRiskScore(90);
        blockedResponse.setFraudReason("Account is not active, KYC is not verified");
        blockedResponse.setStatus(TransactionStatus.BLOCKED);
        blockedResponse.setCreatedAt(LocalDateTime.now());

        when(service.createTransaction(any(TransactionRequest.class)))
                .thenReturn(blockedResponse);

        FraudCheckResponse res = controller.createTransaction(validRequest);

        assertNotNull(res);
        assertTrue(res.isFraudDetected());
        assertEquals(90, res.getRiskScore());
        assertEquals(TransactionStatus.BLOCKED, res.getStatus());
    }

    @Test
    void detectFraud_validRequest_returnsFraudCheckResponse() {
        when(service.detectFraud(any(TransactionRequest.class)))
                .thenReturn(successResponse);

        FraudCheckResponse res = controller.detectFraud(validRequest);

        assertNotNull(res);
        assertFalse(res.isFraudDetected());
        assertEquals(0, res.getRiskScore());
        assertEquals(TransactionStatus.SUCCESS, res.getStatus());
    }

    @Test
    void detectFraud_detectedFraud_returnsFraudIndicator() {
        FraudCheckResponse fraudResponse = new FraudCheckResponse();
        fraudResponse.setTransactionId("1-fraud-test");
        fraudResponse.setCustomerId(1L);
        fraudResponse.setFraudDetected(true);
        fraudResponse.setRiskScore(65);
        fraudResponse.setFraudReason("KYC is not verified, Account is not active");
        fraudResponse.setStatus(TransactionStatus.BLOCKED);
        fraudResponse.setCreatedAt(LocalDateTime.now());

        when(service.detectFraud(any(TransactionRequest.class)))
                .thenReturn(fraudResponse);

        FraudCheckResponse res = controller.detectFraud(validRequest);

        assertNotNull(res);
        assertTrue(res.isFraudDetected());
        assertEquals(65, res.getRiskScore());
        assertEquals(TransactionStatus.BLOCKED, res.getStatus());
    }

    @Test
    void createTransaction_runtimeException_throwsException() {
        when(service.createTransaction(any(TransactionRequest.class)))
                .thenThrow(new RuntimeException("Customer not found"));

        assertThrows(RuntimeException.class, () -> {
            controller.createTransaction(validRequest);
        });
    }

    @Test
    void detectFraud_withMultipleChannels_succeeds() {
        TransactionRequest upiRequest = new TransactionRequest();
        upiRequest.setCustomerId(1L);
        upiRequest.setAmount(new BigDecimal("1000"));
        upiRequest.setChannel(ChannelType.UPI);
        upiRequest.setTransactionType(TransactionType.CREDITED);

        when(service.detectFraud(any(TransactionRequest.class)))
                .thenReturn(successResponse);

        FraudCheckResponse res = controller.detectFraud(upiRequest);

        assertNotNull(res);
        assertEquals(TransactionStatus.SUCCESS, res.getStatus());
    }

    @Test
    void createTransaction_atmChannel_succeeds() {
        TransactionRequest atmRequest = new TransactionRequest();
        atmRequest.setCustomerId(2L);
        atmRequest.setAmount(new BigDecimal("2000"));
        atmRequest.setChannel(ChannelType.ATM);
        atmRequest.setTransactionType(TransactionType.DEBITED);

        FraudCheckResponse atmResponse = new FraudCheckResponse();
        atmResponse.setTransactionId("2-atm-test");
        atmResponse.setCustomerId(2L);
        atmResponse.setFraudDetected(false);
        atmResponse.setRiskScore(0);
        atmResponse.setFraudReason("No fraud indicators detected");
        atmResponse.setStatus(TransactionStatus.SUCCESS);
        atmResponse.setCreatedAt(LocalDateTime.now());

        when(service.createTransaction(any(TransactionRequest.class)))
                .thenReturn(atmResponse);

        FraudCheckResponse res = controller.createTransaction(atmRequest);

        assertNotNull(res);
        assertEquals(2L, res.getCustomerId());
        assertEquals(TransactionStatus.SUCCESS, res.getStatus());
    }

}






