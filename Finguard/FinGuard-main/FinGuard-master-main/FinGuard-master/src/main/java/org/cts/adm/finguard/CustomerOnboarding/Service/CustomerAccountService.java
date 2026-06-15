package org.cts.adm.finguard.CustomerOnboarding.Service;

import org.cts.adm.finguard.CustomerOnboarding.Eunm.AccountStatus;
import org.cts.adm.finguard.CustomerOnboarding.Eunm.KycStatus;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerAccountService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerAccountService.class);

    private final CustomerRepository customerRepository;

    public CustomerAccountService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public AccountStatus updateAccountStatus(Long customerId, AccountStatus newStatus) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        AccountStatus currentStatus = customer.getAccountStatus();

        if (currentStatus == newStatus) {
            return currentStatus;
        }

        validateTransition(customer, currentStatus, newStatus);

        customer.setAccountStatus(newStatus);
        customerRepository.save(customer);

        logger.info("Account status updated for customerId={} from {} to {}",
                customerId, currentStatus, newStatus);

        return newStatus;
    }

    private void validateTransition(Customer customer,
                                    AccountStatus currentStatus,
                                    AccountStatus newStatus) {
        if (currentStatus == AccountStatus.CLOSED && newStatus != AccountStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Closed account cannot be reactivated");
        }

        if (newStatus == AccountStatus.ACTIVE && customer.getKycStatus() != KycStatus.VERIFIED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Account can be activated only after KYC is VERIFIED");
        }

        boolean allowed = switch (currentStatus) {
            case PENDING -> newStatus == AccountStatus.ACTIVE
                    || newStatus == AccountStatus.SUSPENDED
                    || newStatus == AccountStatus.CLOSED;
            case ACTIVE -> newStatus == AccountStatus.SUSPENDED
                    || newStatus == AccountStatus.CLOSED;
            case SUSPENDED -> newStatus == AccountStatus.ACTIVE
                    || newStatus == AccountStatus.CLOSED;
            case CLOSED -> newStatus == AccountStatus.CLOSED;
        };

        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid account status transition from " + currentStatus + " to " + newStatus);
        }
    }
}

