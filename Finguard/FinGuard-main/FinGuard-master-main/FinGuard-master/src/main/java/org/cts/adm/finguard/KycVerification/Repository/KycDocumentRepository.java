package org.cts.adm.finguard.KycVerification.Repository;

import org.cts.adm.finguard.KycVerification.Model.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KycDocumentRepository extends JpaRepository<KycDocument,Long> {
    Optional<KycDocument> findByCustomerCustomerId(Long customerId);

}
