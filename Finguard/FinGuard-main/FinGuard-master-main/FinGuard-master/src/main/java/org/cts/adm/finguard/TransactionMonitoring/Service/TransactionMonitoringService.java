package org.cts.adm.finguard.TransactionMonitoring.Service;

import org.cts.adm.finguard.CustomerOnboarding.Eunm.AccountStatus;
import org.cts.adm.finguard.CustomerOnboarding.Eunm.KycStatus;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Service.CustomerLoginService;
import org.cts.adm.finguard.RiskAlert.Service.RiskAlertService;
import org.cts.adm.finguard.TransactionMonitoring.Dto.FraudCheckResponse;
import org.cts.adm.finguard.TransactionMonitoring.Dto.TransactionRequest;
import org.cts.adm.finguard.TransactionMonitoring.Enum.ChannelType;
import org.cts.adm.finguard.TransactionMonitoring.Enum.TransactionStatus;
import org.cts.adm.finguard.TransactionMonitoring.Model.Transaction;
import org.cts.adm.finguard.TransactionMonitoring.Repository.TransactionMonitoringRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionMonitoringService {

    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("50000");
    private static final BigDecimal ATM_HIGH_VALUE_THRESHOLD = new BigDecimal("20000");
    private static final int FLAG_THRESHOLD = 30;
    private static final int BLOCK_THRESHOLD = 60;

    public final TransactionMonitoringRepository transactionMonitoringRepository;
    public final CustomerLoginService customerLoginService;
    private final RiskAlertService riskAlertService;

    TransactionMonitoringService (TransactionMonitoringRepository transactionMonitoringRepository,
                                  CustomerLoginService customerLoginService
    ,RiskAlertService riskAlertService){
        this.transactionMonitoringRepository = transactionMonitoringRepository;
        this.customerLoginService = customerLoginService;
        this.riskAlertService = riskAlertService;
    }

    public FraudCheckResponse createTransaction(TransactionRequest transactionRequest){
        try {
            Transaction transaction = buildTransaction(transactionRequest);
            String customId = transactionRequest.getCustomerId() + "-" + UUID.randomUUID().toString();
            transaction.setTransactionId(customId);
            FraudCheckResponse result = evaluateFraud(transaction);
            Transaction savedTransaction = transactionMonitoringRepository.save(transaction);
            result.setTransactionId(savedTransaction.getTransactionId());

            result.setCreatedAt(savedTransaction.getCreatedAt());
            riskAlertService.evaluateAndCreateAlert(savedTransaction);
            return result;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }


    public FraudCheckResponse detectFraud(TransactionRequest transactionRequest) {
        Transaction transaction = buildTransaction(transactionRequest);

        return evaluateFraud(transaction);
    }

    private Transaction buildTransaction(TransactionRequest transactionRequest) {
        if (transactionRequest.getCustomerId() == null) {
            throw new RuntimeException("Customer id is required");
        }

        Customer customer = customerLoginService.getCustomerById(transactionRequest.getCustomerId());
        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }

        if (transactionRequest.getAmount() == null) {
            throw new RuntimeException("Transaction amount is required");
        }

        if (transactionRequest.getChannel() == null) {
            throw new RuntimeException("Transaction channel is required");
        }

        if (transactionRequest.getTransactionType() == null) {
            throw new RuntimeException("Transaction type is required");
        }

        Transaction transaction = new Transaction();
        transaction.setCustomer(customer);
        transaction.setAmount(transactionRequest.getAmount());
        transaction.setChannel(transactionRequest.getChannel());
        transaction.setTransactionType(transactionRequest.getTransactionType());
        transaction.setCreatedAt(LocalDateTime.now());
        return transaction;
    }

    private FraudCheckResponse evaluateFraud(Transaction transaction) {
        Customer customer = transaction.getCustomer();
        if (customer == null || customer.getCustomerId() == null) {
            transaction.setRiskScore(BLOCK_THRESHOLD);
            transaction.setFraudReason("Customer information is missing");
            transaction.setStatus(TransactionStatus.BLOCKED);
            return toResponse(transaction, true);
        }

        int riskScore = 0;
        List<String> reasons = new ArrayList<>();

        if (customer.getKycStatus() != KycStatus.VERIFIED) {
            riskScore += 40;
            reasons.add("KYC is not verified");
        }

        if (customer.getAccountStatus() != AccountStatus.ACTIVE) {
            riskScore += 50;
            reasons.add("Account is not active");
        }

        if (transaction.getAmount() != null &&
                transaction.getAmount().compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            riskScore += 30;
            reasons.add("High-value transaction above " + HIGH_VALUE_THRESHOLD);
        }

        if (transaction.getAmount() != null &&
                transaction.getChannel() == ChannelType.ATM &&
                transaction.getAmount().compareTo(ATM_HIGH_VALUE_THRESHOLD) > 0) {
            riskScore += 20;
            reasons.add("High-value ATM transaction");
        }

        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        long recentTransactions = transactionMonitoringRepository
                .countByCustomerCustomerIdAndCreatedAtAfter(customer.getCustomerId(), fiveMinutesAgo);
        if (recentTransactions >= 3) {
            riskScore += 30;
            reasons.add("High transaction frequency in the last 5 minutes");
        }

        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        long recentFlaggedTransactions = transactionMonitoringRepository
                .countByCustomerCustomerIdAndStatusAndCreatedAtAfter(
                        customer.getCustomerId(),
                        TransactionStatus.FLAGGED,
                        oneDayAgo
                );
        if (recentFlaggedTransactions >= 2) {
            riskScore += 20;
            reasons.add("Customer has repeated flagged transactions");
        }

        if (!customer.isMfaEnabled() &&
                transaction.getAmount() != null &&
                transaction.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            riskScore += 10;
            reasons.add("MFA is disabled for a significant transaction");
        }

        transaction.setRiskScore(riskScore);

        if (riskScore >= BLOCK_THRESHOLD) {
            transaction.setStatus(TransactionStatus.BLOCKED);
        } else if (riskScore >= FLAG_THRESHOLD) {
            transaction.setStatus(TransactionStatus.FLAGGED);
        } else {
            transaction.setStatus(TransactionStatus.SUCCESS);
        }

        if (reasons.isEmpty()) {
            transaction.setFraudReason("No fraud indicators detected");
        } else {
            transaction.setFraudReason(String.join(", ", reasons));
        }

        return toResponse(transaction, riskScore >= FLAG_THRESHOLD);
    }

    private FraudCheckResponse toResponse(Transaction transaction, boolean fraudDetected) {
        FraudCheckResponse response = new FraudCheckResponse();
        response.setTransactionId(transaction.getTransactionId());
        if (transaction.getCustomer() != null) {
            response.setCustomerId(transaction.getCustomer().getCustomerId());
        }
        response.setFraudDetected(fraudDetected);
        response.setRiskScore(transaction.getRiskScore());
        response.setFraudReason(transaction.getFraudReason());
        response.setStatus(transaction.getStatus());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }

}
