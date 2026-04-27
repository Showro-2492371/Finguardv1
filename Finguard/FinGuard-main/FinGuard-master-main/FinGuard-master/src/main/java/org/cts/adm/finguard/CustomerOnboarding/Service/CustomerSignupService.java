package org.cts.adm.finguard.CustomerOnboarding.Service;

import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.cts.adm.finguard.KycVerification.Service.KycVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CustomerSignupService {

    private final CustomerRepository customerRepository;

    public CustomerSignupService(CustomerRepository customerRepository,
                                 KycVerificationService kycVerificationService){
        this.customerRepository = customerRepository;
    }

    public void SignUp(Customer customer){
        try{
            ResponseEntity.ok(customerRepository.save(customer));
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
