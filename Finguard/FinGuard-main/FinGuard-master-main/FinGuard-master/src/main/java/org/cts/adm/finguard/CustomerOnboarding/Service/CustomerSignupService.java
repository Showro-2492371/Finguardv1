package org.cts.adm.finguard.CustomerOnboarding.Service;

import org.cts.adm.finguard.CustomerOnboarding.Exception.DuplicateContactInfoException;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.cts.adm.finguard.CustomerOnboarding.Dto.CustomerSignupRequest;

@Service
public class CustomerSignupService {

    private static final Logger logger =
            LoggerFactory.getLogger(CustomerSignupService.class);

    private final CustomerRepository customerRepository;

    public CustomerSignupService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void SignUp(Customer customer) {

        logger.info("Customer signup process started for name={}", customer.getName());
        logger.debug("Saving customer details to database");

        String contactInfo = customer.getContactInfo();
        if (contactInfo != null) {
            contactInfo = contactInfo.trim();
            customer.setContactInfo(contactInfo);
        }

        if (contactInfo != null && customerRepository.existsByContactInfo(contactInfo)) {
            logger.warn("Signup blocked due to duplicate contactInfo={}", contactInfo);
            throw new DuplicateContactInfoException("Contact already registered. Please login instead.");
        }

        try {
            customerRepository.save(customer);
            logger.info("Customer signup successful for name={}", customer.getName());
        } catch (RuntimeException e) {
            logger.error("Customer signup failed for name={}", customer.getName(), e);
            throw e;
        }
    }

    // Overload used by tests: accept DTO and map to entity
    public void signUp(CustomerSignupRequest request) {
        Customer c = new Customer();
        c.setName(request.getName());
        c.setContactInfo(request.getContactInfo());
        c.setPassword(request.getPassword());
        c.setMfaEnabled(request.isMfaEnabled());
        // delegate to existing save flow
        SignUp(c);
    }
}