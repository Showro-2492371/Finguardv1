package org.cts.adm.finguard.TransactionMonitoring.Repository;

import org.cts.adm.finguard.TransactionMonitoring.Enum.TransactionStatus;
import org.cts.adm.finguard.TransactionMonitoring.Model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionMonitoringRepository extends JpaRepository<Transaction, String> {

    long countByCustomerCustomerIdAndCreatedAtAfter(Long customerId, LocalDateTime createdAt);

    long countByCustomerCustomerIdAndStatusAndCreatedAtAfter(Long customerId,
                                                             TransactionStatus status,
                                                             LocalDateTime createdAt);

    int countByStatus(TransactionStatus status);

    /** Fetch all transactions for a given customer, newest first */
    List<Transaction> findByCustomerCustomerIdOrderByCreatedAtDesc(Long customerId);

    /** Count all transactions by status – used by analytics */
    long countByCustomerCustomerIdAndStatus(Long customerId, TransactionStatus status);
}
