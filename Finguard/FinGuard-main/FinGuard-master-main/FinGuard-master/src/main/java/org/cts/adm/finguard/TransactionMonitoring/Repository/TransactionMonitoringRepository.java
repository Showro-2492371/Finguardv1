package org.cts.adm.finguard.TransactionMonitoring.Repository;

import org.cts.adm.finguard.TransactionMonitoring.Enum.TransactionStatus;
import org.cts.adm.finguard.TransactionMonitoring.Model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TransactionMonitoringRepository extends JpaRepository<Transaction,
        Long> {

    long countByCustomerCustomerIdAndCreatedAtAfter(Long customerId, LocalDateTime createdAt);

    long countByCustomerCustomerIdAndStatusAndCreatedAtAfter(Long customerId,
                                                             TransactionStatus status,
                                                             LocalDateTime createdAt);
}
