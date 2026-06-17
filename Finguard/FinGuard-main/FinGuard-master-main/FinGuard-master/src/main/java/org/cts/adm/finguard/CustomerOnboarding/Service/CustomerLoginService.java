package org.cts.adm.finguard.CustomerOnboarding.Service;

import org.cts.adm.finguard.CustomerOnboarding.Eunm.AccountStatus;
import org.cts.adm.finguard.CustomerOnboarding.Eunm.Role;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.cts.adm.finguard.Jwt.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CustomerLoginService {

    private static final Logger logger =
            LoggerFactory.getLogger(CustomerLoginService.class);

    private final CustomerRepository customerRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public CustomerLoginService(CustomerRepository customerRepository,
                                JwtUtil jwtUtil,
                                PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public String login(String name, String password) {

        List<Customer> customers = customerRepository.findAllByName(name);

        if (customers.isEmpty()) {
            logger.warn("Login failed: customer not found for name={}", name);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // Use BCrypt's constant-time matches() – prevents timing attacks
        Customer customer = customers.stream()
                .filter(candidate -> credentialMatches(candidate, password))
                .findFirst()
                .orElseThrow(() -> {
                    logger.warn("Login failed: invalid password for name={}", name);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });

        migrateLegacyPasswordIfNeeded(customer, password);

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


    public Customer getCustomerByName(String name) {
        logger.debug("Fetching customer by name={}", name);
        List<Customer> customers = customerRepository.findAllByName(name);
        return customers.isEmpty() ? null : customers.get(0);
    }

    public Customer getCustomerById(Long id) {
        logger.debug("Fetching customer by id={}", id);
        return customerRepository.findCustomerByCustomerId(id);
    }

    private boolean credentialMatches(Customer candidate, String rawPassword) {
        String storedPassword = candidate.getPassword();
        if (storedPassword == null || rawPassword == null) {
            return false;
        }

        if (isBcryptHash(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        // Legacy fallback for historical plaintext rows.
        return rawPassword.equals(storedPassword);
    }

    private void migrateLegacyPasswordIfNeeded(Customer customer, String rawPassword) {
        String storedPassword = customer.getPassword();
        if (storedPassword == null || isBcryptHash(storedPassword)) {
            return;
        }

        customer.setPassword(passwordEncoder.encode(rawPassword));
        customerRepository.save(customer);
        logger.info("Migrated legacy plaintext password to BCrypt for customerId={}", customer.getCustomerId());
    }

    private boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}