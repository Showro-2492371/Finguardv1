package org.cts.adm.finguard.TransactionMonitoring.Service;

import org.cts.adm.finguard.CustomerOnboarding.Eunm.AccountStatus;
import org.cts.adm.finguard.CustomerOnboarding.Eunm.KycStatus;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.RiskAlert.Service.RiskAlertService;
import org.cts.adm.finguard.TransactionMonitoring.Dto.FraudCheckResponse;
import org.cts.adm.finguard.TransactionMonitoring.Dto.TransactionRequest;
import org.cts.adm.finguard.TransactionMonitoring.Enum.ChannelType;
import org.cts.adm.finguard.TransactionMonitoring.Enum.TransactionStatus;
import org.cts.adm.finguard.TransactionMonitoring.Enum.TransactionType;
import org.cts.adm.finguard.TransactionMonitoring.Model.Transaction;
import org.cts.adm.finguard.TransactionMonitoring.Repository.TransactionMonitoringRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionMonitoringServiceTest {

    @Mock
    private TransactionMonitoringRepository repository;

    @Mock
    private org.cts.adm.finguard.CustomerOnboarding.Service.CustomerLoginService customerLoginService;

    @Mock
    private RiskAlertService riskAlertService;

    @InjectMocks
    private TransactionMonitoringService service;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setCustomerId(1L);
        customer.setKycStatus(KycStatus.VERIFIED);
        customer.setAccountStatus(AccountStatus.ACTIVE);
        customer.setMfaEnabled(true);
    }

    @Test
    void detectFraud_lowRisk_success() {
        TransactionRequest req = new TransactionRequest();
        req.setCustomerId(1L);
        req.setAmount(new BigDecimal("100"));
        req.setChannel(ChannelType.ONLINE_BANKING);
        req.setTransactionType(TransactionType.DEBITED);

        when(customerLoginService.getCustomerById(1L)).thenReturn(customer);
        when(repository.countByCustomerCustomerIdAndCreatedAtAfter(anyLong(), any())).thenReturn(0L);
        when(repository.countByCustomerCustomerIdAndStatusAndCreatedAtAfter(anyLong(), any(), any())).thenReturn(0L);

        FraudCheckResponse res = service.detectFraud(req);

        assertNotNull(res);
        assertFalse(res.isFraudDetected());
        assertEquals(0, res.getRiskScore());
        assertEquals(TransactionStatus.SUCCESS, res.getStatus());
    }

    @Test
    void detectFraud_highValue_flagged() {
        TransactionRequest req = new TransactionRequest();
        req.setCustomerId(1L);
        req.setAmount(new BigDecimal("60000"));
        req.setChannel(ChannelType.ONLINE_BANKING);
        req.setTransactionType(TransactionType.CREDITED);

        when(customerLoginService.getCustomerById(1L)).thenReturn(customer);
        when(repository.countByCustomerCustomerIdAndCreatedAtAfter(anyLong(), any())).thenReturn(0L);
        when(repository.countByCustomerCustomerIdAndStatusAndCreatedAtAfter(anyLong(), any(), any())).thenReturn(0L);

        FraudCheckResponse res = service.detectFraud(req);

        assertNotNull(res);
        assertTrue(res.isFraudDetected());
        assertEquals(30, res.getRiskScore());
        assertEquals(TransactionStatus.FLAGGED, res.getStatus());
    }

    @Test
    void detectFraud_blocked_dueToKycAndAccount() {
        TransactionRequest req = new TransactionRequest();
        req.setCustomerId(1L);
        req.setAmount(new BigDecimal("1000"));
        req.setChannel(ChannelType.UPI);
        req.setTransactionType(TransactionType.DEBITED);

        // make customer high risk: KYC not verified and account not active
        customer.setKycStatus(KycStatus.NOT_STARTED);
        customer.setAccountStatus(AccountStatus.PENDING);

        when(customerLoginService.getCustomerById(1L)).thenReturn(customer);
        when(repository.countByCustomerCustomerIdAndCreatedAtAfter(anyLong(), any())).thenReturn(0L);
        when(repository.countByCustomerCustomerIdAndStatusAndCreatedAtAfter(anyLong(), any(), any())).thenReturn(0L);

        FraudCheckResponse res = service.detectFraud(req);

        assertNotNull(res);
        assertTrue(res.isFraudDetected());
        assertTrue(res.getRiskScore() >= 60);
        assertEquals(TransactionStatus.BLOCKED, res.getStatus());
    }

    @Test
    void createTransaction_savesAndReturns() {
        TransactionRequest req = new TransactionRequest();
        req.setCustomerId(1L);
        req.setAmount(new BigDecimal("200"));
        req.setChannel(ChannelType.ONLINE_BANKING);
        req.setTransactionType(TransactionType.CREDITED);

        when(customerLoginService.getCustomerById(1L)).thenReturn(customer);
        when(repository.countByCustomerCustomerIdAndCreatedAtAfter(anyLong(), any())).thenReturn(0L);
        when(repository.countByCustomerCustomerIdAndStatusAndCreatedAtAfter(anyLong(), any(), any())).thenReturn(0L);

        when(repository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setTransactionId(req.getCustomerId() + "-test-id");
            t.setCreatedAt(LocalDateTime.now());
            return t;
        });

        FraudCheckResponse res = service.createTransaction(req);

        assertNotNull(res);
        assertEquals(req.getCustomerId(), res.getCustomerId());
        assertNotNull(res.getTransactionId());
        verify(riskAlertService, times(1)).evaluateAndCreateAlert(any(Transaction.class));
    }

}

