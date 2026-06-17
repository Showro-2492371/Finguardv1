package org.cts.adm.finguard.Analytics.Repository;

import org.cts.adm.finguard.Analytics.Model.FraudAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudAnalyticsRepository extends JpaRepository<FraudAnalytics, Long> {

    /** Returns the latest N analytics snapshots ordered by generated date */
    List<FraudAnalytics> findTop12ByOrderByGeneratedDateDesc();
}

