package org.cts.adm.finguard.CustomerOnboarding.Controller;

import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.cts.adm.finguard.CustomerOnboarding.Service.CustomerSignupService;
import org.cts.adm.finguard.KycVerification.Service.KycVerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/signup")
public class CustomerSignupController {

    private final CustomerRepository customerRepository;
    private final CustomerSignupService customerSignupService;

     CustomerSignupController(CustomerRepository customerRepository,
                             CustomerSignupService customerSignupService
                            ){

        this.customerRepository = customerRepository;
        this.customerSignupService = customerSignupService;
         }

    @PostMapping
    public void createCustomer(@RequestBody Customer customer) {
        try{
            customerSignupService.SignUp(customer);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

    }

}
