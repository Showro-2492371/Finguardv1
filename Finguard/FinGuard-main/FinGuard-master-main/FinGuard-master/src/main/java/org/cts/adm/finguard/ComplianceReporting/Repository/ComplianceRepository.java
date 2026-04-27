package org.cts.adm.finguard.ComplianceReporting.Repository;

import org.cts.adm.finguard.ComplianceReporting.Model.ComplianceReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplianceRepository extends JpaRepository<ComplianceReport, Long> {
}
