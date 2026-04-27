package org.cts.adm.finguard.TransactionMonitoring.Controller;

import org.cts.adm.finguard.TransactionMonitoring.Dto.FraudCheckResponse;
import org.cts.adm.finguard.TransactionMonitoring.Dto.TransactionRequest;
import org.cts.adm.finguard.TransactionMonitoring.Service.TransactionMonitoringService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/transaction")
public class TransactionMonitoringController {

    public final TransactionMonitoringService transactionMonitoringService;

    TransactionMonitoringController(TransactionMonitoringService transactionMonitoringService){
        this.transactionMonitoringService = transactionMonitoringService;
    }

    @PostMapping("/add")
    public FraudCheckResponse createTransaction(@RequestBody TransactionRequest transactionRequest){
        try{
            return transactionMonitoringService.createTransaction(transactionRequest);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/detectFraud")
    public FraudCheckResponse detectFraud(@RequestBody TransactionRequest transactionRequest){
        return transactionMonitoringService.detectFraud(transactionRequest);
    }

}
