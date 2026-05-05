package org.cts.adm.finguard.CustomerOnboarding.Controller;

import org.cts.adm.finguard.CustomerOnboarding.Dto.LoginRequest;
import org.cts.adm.finguard.CustomerOnboarding.Service.CustomerLoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerLoginController {

    private static final Logger logger =
            LoggerFactory.getLogger(CustomerLoginController.class);

    private final CustomerLoginService customerLoginService;

    public CustomerLoginController(CustomerLoginService customerLoginService) {
        this.customerLoginService = customerLoginService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        logger.info("Login request received for customer name={}", request.getName());

        // NEVER log password
        logger.debug("Processing login request");

        String jwtToken = customerLoginService.login(
                request.getName(),
                request.getPassword()
        );

        logger.info("Login successful for customer name={}", request.getName());

        return ResponseEntity.ok(jwtToken);
    }
}