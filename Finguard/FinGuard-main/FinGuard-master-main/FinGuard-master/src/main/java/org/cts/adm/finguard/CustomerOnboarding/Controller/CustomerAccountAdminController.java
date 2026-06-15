package org.cts.adm.finguard.CustomerOnboarding.Controller;

import org.cts.adm.finguard.CustomerOnboarding.Eunm.AccountStatus;
import org.cts.adm.finguard.CustomerOnboarding.Service.CustomerAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/customer")
public class CustomerAccountAdminController {

    private final CustomerAccountService customerAccountService;

    public CustomerAccountAdminController(CustomerAccountService customerAccountService) {
        this.customerAccountService = customerAccountService;
    }

    @PutMapping("/account-status")
    public ResponseEntity<String> updateAccountStatus(@RequestParam Long customerId,
                                                      @RequestParam String status) {
        try {
            AccountStatus accountStatus = AccountStatus.valueOf(status.toUpperCase());
            AccountStatus updatedStatus = customerAccountService.updateAccountStatus(customerId, accountStatus);
            return ResponseEntity.ok("Account status updated to " + updatedStatus);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        }
    }
}


