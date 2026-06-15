package org.cts.adm.finguard.KycVerification.Repository;

import org.cts.adm.finguard.KycVerification.Model.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument,Long> {
    Optional<KycDocument> findByCustomerCustomerId(Long customerId);

    List<KycDocument> findAllByOrderByUploadedAtDesc();

}
