package org.cts.adm.finguard.ComplianceReporting.Repository;

import jakarta.transaction.Transactional;
import org.cts.adm.finguard.ComplianceReporting.Model.ComplianceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;
import java.util.List;

public interface ComplianceRepository extends JpaRepository<ComplianceReport, Long> {

    List<ComplianceReport> findByCustomerId(Long customerId);

    @Transactional
    @Modifying
    void deleteByCustomerId(Long customerId);

    List<ComplianceReport> findByGeneratedDateBetween(LocalDateTime start,LocalDateTime end);
}
