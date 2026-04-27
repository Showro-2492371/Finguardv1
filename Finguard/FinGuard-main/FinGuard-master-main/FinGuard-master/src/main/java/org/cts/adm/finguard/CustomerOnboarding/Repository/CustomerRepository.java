package org.cts.adm.finguard.CustomerOnboarding.Repository;

import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CustomerRepository extends
        JpaRepository<Customer,Long> {

   Optional<Customer> findCustomersByName(String name);

   Customer findCustomerByCustomerId(Long customerId);

   boolean existsByCustomerId(Long customerId);
}
