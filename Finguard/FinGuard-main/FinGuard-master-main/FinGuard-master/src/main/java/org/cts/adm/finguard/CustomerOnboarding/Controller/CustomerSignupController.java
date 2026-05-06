package org.cts.adm.finguard.CustomerOnboarding.Controller;

import org.cts.adm.finguard.CustomerOnboarding.Eunm.Role;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.cts.adm.finguard.CustomerOnboarding.Service.CustomerSignupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerSignupController {

    private static final Logger logger =
            LoggerFactory.getLogger(CustomerSignupController.class);

    private final CustomerRepository customerRepository;
    private final CustomerSignupService customerSignupService;

    public CustomerSignupController(CustomerRepository customerRepository,
                                    CustomerSignupService customerSignupService) {
        this.customerRepository = customerRepository;
        this.customerSignupService = customerSignupService;
    }

    @PostMapping("/signup")
    public void createCustomer(@RequestBody Customer customer) {

        logger.info("Customer signup request received for name={}", customer.getName());
        logger.debug("Processing customer signup");

        try {
            // ✅ Default role assignment
            customer.setRole(Role.ROLE_USER);

            customerSignupService.SignUp(customer);

            logger.info("Customer signup successful for name={}", customer.getName());
        } catch (RuntimeException e) {
            logger.error("Customer signup failed for name={}", customer.getName(), e);
            throw e;
        }
    }

//    @PostMapping("/signup")
//    public void createCustomer(@RequestBody Customer customer) {
//
//        logger.info("Customer signup request received for name={}", customer.getName());
//        logger.debug("Processing customer signup");
//
//        try {
//            customerSignupService.SignUp(customer);
//            logger.info("Customer signup successful for name={}", customer.getName());
//        } catch (RuntimeException e) {
//            logger.error("Customer signup failed for name={}", customer.getName(), e);
//            throw e;
//        }
//    }
}