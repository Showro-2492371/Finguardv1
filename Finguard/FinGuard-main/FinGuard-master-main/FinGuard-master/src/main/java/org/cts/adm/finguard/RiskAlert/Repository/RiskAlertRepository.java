package org.cts.adm.finguard.RiskAlert.Repository;

import org.cts.adm.finguard.RiskAlert.Enum.RiskAlertStatus;
import org.cts.adm.finguard.RiskAlert.Model.RiskAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.Collection;

@Repository
public interface RiskAlertRepository extends JpaRepository<RiskAlert, Long> {
    Optional<RiskAlert> findByTransactionId(String transactionId);
    List<RiskAlert> findByStatus(RiskAlertStatus status);
    List<RiskAlert> findByStatusIn(Collection<RiskAlertStatus> statuses);
}
