package org.cts.adm.finguard.TransactionMonitoring.Controller;

import org.cts.adm.finguard.TransactionMonitoring.Dto.FraudCheckResponse;
import org.cts.adm.finguard.TransactionMonitoring.Dto.TransactionRequest;
import org.cts.adm.finguard.TransactionMonitoring.Service.TransactionMonitoringService;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/customer/transaction")
public class TransactionMonitoringController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionMonitoringController.class);

    public final TransactionMonitoringService transactionMonitoringService;

    TransactionMonitoringController(TransactionMonitoringService transactionMonitoringService){
        this.transactionMonitoringService = transactionMonitoringService;
    }

    @PostMapping("/add")
    public FraudCheckResponse createTransaction(@RequestBody TransactionRequest transactionRequest){
        logger.info("Received createTransaction request for customerId={}", transactionRequest.getCustomerId());
        try{
            FraudCheckResponse response = transactionMonitoringService.createTransaction(transactionRequest);
            logger.info("createTransaction completed: transactionId={} status={}", response.getTransactionId(), response.getStatus());
            return response;
        } catch (RuntimeException e) {
            logger.error("Error in createTransaction for customerId={}", transactionRequest.getCustomerId(), e);
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/detectFraud")
    public FraudCheckResponse detectFraud(@RequestBody TransactionRequest transactionRequest){
        logger.info("Received detectFraud request for customerId={}", transactionRequest.getCustomerId());
        FraudCheckResponse response = transactionMonitoringService.detectFraud(transactionRequest);
        logger.info("detectFraud completed: customerId={} status={} riskScore={}", response.getCustomerId(), response.getStatus(), response.getRiskScore());
        return response;
    }

}
