package org.cts.adm.finguard.ComplianceReporting.Repository;

import org.cts.adm.finguard.ComplianceReporting.Model.AuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {
}