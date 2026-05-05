package org.cts.adm.finguard.CustomerOnboarding.Service;

import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.cts.adm.finguard.Jwt.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CustomerLoginService {

    private static final Logger logger =
            LoggerFactory.getLogger(CustomerLoginService.class);

    private final CustomerRepository customerRepository;
    private final JwtUtil jwtUtil;

    public CustomerLoginService(CustomerRepository customerRepository,
                                JwtUtil jwtUtil) {
        this.customerRepository = customerRepository;
        this.jwtUtil = jwtUtil;
    }

    public String login(String name, String password) {

        logger.info("Login attempt started for customer name={}", name);

        Customer customer = customerRepository.findCustomersByName(name)
                .orElseThrow(() -> {
                    logger.warn("Login failed: user not found for name={}", name);
                    return new RuntimeException("User not found");
                });

        if (!password.equals(customer.getPassword())) {
            logger.warn("Login failed: invalid password for name={}", name);
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(name);

        logger.info("Login successful for customer name={}", name);
        return token;
    }

    public Customer getCustomerByName(String name) {
        logger.debug("Fetching customer by name={}", name);
        return customerRepository
                .findCustomersByName(name)
                .orElse(null);
    }

    public Customer getCustomerById(Long id) {
        logger.debug("Fetching customer by id={}", id);
        return customerRepository.findCustomerByCustomerId(id);
    }
}