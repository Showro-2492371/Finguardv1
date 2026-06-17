package org.cts.adm.finguard.CustomerOnboarding.Controller;

import jakarta.validation.Valid;
import org.cts.adm.finguard.CustomerOnboarding.Dto.CustomerSignupRequest;
import org.cts.adm.finguard.CustomerOnboarding.Service.CustomerSignupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles customer self-registration.
 * Accepts a validated DTO (NOT a raw entity) to prevent privilege escalation
 * (e.g., a client cannot set their own role or accountStatus via the API).
 */
@RestController
@RequestMapping("/api/customer")
public class CustomerSignupController {

    private static final Logger logger =
            LoggerFactory.getLogger(CustomerSignupController.class);

    private final CustomerSignupService customerSignupService;

    public CustomerSignupController(CustomerSignupService customerSignupService) {
        this.customerSignupService = customerSignupService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> createCustomer(@Valid @RequestBody CustomerSignupRequest request) {

        logger.info("Customer signup request received for name={}", request.getName());

        try {
            customerSignupService.signUp(request);
            logger.info("Customer signup successful for name={}", request.getName());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Customer registered successfully");
        } catch (RuntimeException e) {
            logger.error("Customer signup failed for name={}", request.getName(), e);
            throw e;
        }
    }
}