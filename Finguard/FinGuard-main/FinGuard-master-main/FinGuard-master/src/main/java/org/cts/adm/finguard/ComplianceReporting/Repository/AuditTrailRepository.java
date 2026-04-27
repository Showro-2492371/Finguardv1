package org.cts.adm.finguard.ComplianceReporting.Repository;

import org.cts.adm.finguard.ComplianceReporting.Model.AuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {
}