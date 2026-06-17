package org.cts.adm.finguard.CustomerOnboarding.Service;

import org.cts.adm.finguard.CustomerOnboarding.Dto.CustomerSignupRequest;
import org.cts.adm.finguard.CustomerOnboarding.Exception.DuplicateContactInfoException;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomerSignupService {

    private static final Logger logger =
            LoggerFactory.getLogger(CustomerSignupService.class);

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerSignupService(CustomerRepository customerRepository,
                                  PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new customer. Password is BCrypt-encoded before persistence.
     */
    public void signUp(CustomerSignupRequest request) {
        logger.info("Customer signup process started for name={}", request.getName());

        String contactInfo = request.getContactInfo() != null
                ? request.getContactInfo().trim() : null;

        if (contactInfo != null && customerRepository.existsByContactInfo(contactInfo)) {
            logger.warn("Signup blocked due to duplicate contactInfo={}", contactInfo);
            throw new DuplicateContactInfoException("Contact already registered. Please login instead.");
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setContactInfo(contactInfo);
        // BCrypt encode the password – never store plain text
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setMfaEnabled(request.isMfaEnabled());

        try {
            customerRepository.save(customer);
            logger.info("Customer signup successful for name={}", customer.getName());
        } catch (RuntimeException e) {
            logger.error("Customer signup failed for name={}", customer.getName(), e);
            throw e;
        }
    }

    /** @deprecated use signUp(CustomerSignupRequest) */
    @Deprecated
    public void SignUp(Customer customer) {
        CustomerSignupRequest req = new CustomerSignupRequest();
        req.setName(customer.getName());
        req.setContactInfo(customer.getContactInfo());
        req.setPassword(customer.getPassword());
        req.setMfaEnabled(customer.isMfaEnabled());
        signUp(req);
    }
}