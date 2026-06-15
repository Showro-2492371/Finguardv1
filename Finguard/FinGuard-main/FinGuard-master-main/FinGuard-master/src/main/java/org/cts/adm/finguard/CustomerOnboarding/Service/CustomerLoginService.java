package org.cts.adm.finguard.CustomerOnboarding.Service;

import org.cts.adm.finguard.CustomerOnboarding.Eunm.AccountStatus;
import org.cts.adm.finguard.CustomerOnboarding.Eunm.Role;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.cts.adm.finguard.Jwt.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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

        List<Customer> customers = customerRepository.findAllByName(name);

        if (customers.isEmpty()) {
            logger.warn("Login failed: customer not found for name={}", name);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        Customer customer = customers.stream()
                .filter(candidate -> password.equals(candidate.getPassword()))
                .findFirst()
                .orElseThrow(() -> {
                    logger.warn("Login failed: invalid password for name={}", name);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });

        if (customer.getRole() == Role.ROLE_USER) {
            AccountStatus accountStatus = customer.getAccountStatus() != null
                    ? customer.getAccountStatus()
                    : AccountStatus.PENDING;
            if (accountStatus == AccountStatus.SUSPENDED || accountStatus == AccountStatus.CLOSED) {
                logger.warn("Login blocked: restricted account customerId={} status={}",
                        customer.getCustomerId(), accountStatus);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Account is " + accountStatus + ". Please contact administrator.");
            }
        }

        if (customers.size() > 1) {
            logger.warn("Duplicate customer names found for name={}; using the matching account", name);
        }

        return jwtUtil.generateToken(
                customer.getCustomerId(),
                customer.getName(),
                customer.getRole().name()
        );
    }


//    public String login(String name, String password) {
//
//        logger.info("Login attempt started for customer name={}", name);
//
//        Customer customer = customerRepository.findCustomersByName(name)
//                .orElseThrow(() -> {
//                    logger.warn("Login failed: user not found for name={}", name);
//                    return new RuntimeException("User not found");
//                });
//
//        if (!password.equals(customer.getPassword())) {
//            logger.warn("Login failed: invalid password for name={}", name);
//            throw new RuntimeException("Invalid password");
//        }
//
//        String token = jwtUtil.generateToken(name);
//
//        logger.info("Login successful for customer name={}", name);
//        return token;
//    }

    public Customer getCustomerByName(String name) {
        logger.debug("Fetching customer by name={}", name);
        List<Customer> customers = customerRepository.findAllByName(name);
        return customers.isEmpty() ? null : customers.get(0);
    }

    public Customer getCustomerById(Long id) {
        logger.debug("Fetching customer by id={}", id);
        return customerRepository.findCustomerByCustomerId(id);
    }
}