package org.cts.adm.finguard.CustomerOnboarding.Controller;

import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.cts.adm.finguard.CustomerOnboarding.Service.CustomerLoginService;
import org.cts.adm.finguard.CustomerOnboarding.Dto.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerLoginController {

    private final CustomerLoginService customerLoginService;

    public CustomerLoginController( CustomerLoginService customerLoginService){
        this.customerLoginService = customerLoginService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String jwtToken = customerLoginService.login(
                request.getName(),
                request.getPassword()
        );
        return ResponseEntity.ok(jwtToken);
    }

}
