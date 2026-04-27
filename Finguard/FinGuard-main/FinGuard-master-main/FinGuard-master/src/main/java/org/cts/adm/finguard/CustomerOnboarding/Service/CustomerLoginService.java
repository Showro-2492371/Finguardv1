package org.cts.adm.finguard.CustomerOnboarding.Service;

import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.cts.adm.finguard.Jwt.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerLoginService {

    private final CustomerRepository customerRepository;
    private final JwtUtil jwtUtil;

    public CustomerLoginService(CustomerRepository customerRepository,
                                JwtUtil jwtUtil) {
        this.customerRepository = customerRepository;
        this.jwtUtil = jwtUtil;
    }

    public String login(String name, String password) {

        Customer customer = customerRepository.findCustomersByName(name)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!password.equals(customer.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtUtil.generateToken(name);
    }

    public Customer getCustomerByName(String name) {
        return customerRepository
                .findCustomersByName(name)
                .orElse(null);
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findCustomerByCustomerId(id);
    }
}
