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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionMonitoringService.class);

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
            logger.info("Creating transaction for customerId={} amount={} channel={}",
                    transactionRequest.getCustomerId(), transactionRequest.getAmount(), transactionRequest.getChannel());
            Transaction transaction = buildTransaction(transactionRequest);
            String customId = transactionRequest.getCustomerId() + "-" + UUID.randomUUID().toString();
            transaction.setTransactionId(customId);
            FraudCheckResponse result = evaluateFraud(transaction);
            logger.info("Fraud evaluation result: status={} riskScore={}", result.getStatus(), result.getRiskScore());
            Transaction savedTransaction = transactionMonitoringRepository.save(transaction);
            result.setTransactionId(savedTransaction.getTransactionId());

            result.setCreatedAt(savedTransaction.getCreatedAt());
            logger.info("Transaction saved with id={} for customerId={}", savedTransaction.getTransactionId(), savedTransaction.getCustomer().getCustomerId());
            riskAlertService.evaluateAndCreateAlert(savedTransaction);
            return result;
        } catch (RuntimeException e) {
            logger.error("Error while creating transaction", e);
            throw new RuntimeException(e);
        }
    }


    /**
     * Returns all transactions for the given customer, newest first.
     * Used by the customer portal transaction-history view.
     */
    public List<FraudCheckResponse> getTransactionsByCustomer(Long customerId) {
        logger.info("Fetching transaction history for customerId={}", customerId);
        return transactionMonitoringRepository
                .findByCustomerCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(t -> toResponse(t, t.getRiskScore() != null && t.getRiskScore() >= FLAG_THRESHOLD))
                .toList();
    }

    public FraudCheckResponse detectFraud(TransactionRequest transactionRequest) {
        logger.info("Detecting fraud for customerId={} amount={} channel={}",
                transactionRequest.getCustomerId(), transactionRequest.getAmount(), transactionRequest.getChannel());
        Transaction transaction = buildTransaction(transactionRequest);

        FraudCheckResponse response = evaluateFraud(transaction);
        logger.info("Detect fraud result: customerId={} status={} riskScore={}",
                transaction.getCustomer() != null ? transaction.getCustomer().getCustomerId() : null,
                response.getStatus(), response.getRiskScore());
        return response;
    }

    private Transaction buildTransaction(TransactionRequest transactionRequest) {
        if (transactionRequest.getCustomerId() == null) {
            logger.error("Customer id is missing in transaction request");
            throw new RuntimeException("Customer id is required");
        }

        Customer customer = customerLoginService.getCustomerById(transactionRequest.getCustomerId());
        if (customer == null) {
            logger.error("Customer not found for id={}", transactionRequest.getCustomerId());
            throw new RuntimeException("Customer not found");
        }

        if (transactionRequest.getAmount() == null) {
            logger.error("Transaction amount is missing for customerId={}", transactionRequest.getCustomerId());
            throw new RuntimeException("Transaction amount is required");
        }

        if (transactionRequest.getChannel() == null) {
            logger.error("Transaction channel is missing for customerId={}", transactionRequest.getCustomerId());
            throw new RuntimeException("Transaction channel is required");
        }

        if (transactionRequest.getTransactionType() == null) {
            logger.error("Transaction type is missing for customerId={}", transactionRequest.getCustomerId());
            throw new RuntimeException("Transaction type is required");
        }

        Transaction transaction = new Transaction();
        transaction.setCustomer(customer);
        transaction.setAmount(transactionRequest.getAmount());
        transaction.setChannel(transactionRequest.getChannel());
        transaction.setTransactionType(transactionRequest.getTransactionType());
        transaction.setCreatedAt(LocalDateTime.now());
        logger.debug("Built transaction object for customerId={} amount={} channel={}",
                customer.getCustomerId(), transaction.getAmount(), transaction.getChannel());
        return transaction;
    }

    private FraudCheckResponse evaluateFraud(Transaction transaction) {
        Customer customer = transaction.getCustomer();
        if (customer == null || customer.getCustomerId() == null) {
            transaction.setRiskScore(BLOCK_THRESHOLD);
            transaction.setFraudReason("Customer information is missing");
            transaction.setStatus(TransactionStatus.BLOCKED);
            logger.warn("Blocking transaction because customer information is missing");
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

        logger.info("Evaluated fraud for customerId={} riskScore={} status={} reasons={}",
                customer.getCustomerId(), riskScore, transaction.getStatus(), transaction.getFraudReason());

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
