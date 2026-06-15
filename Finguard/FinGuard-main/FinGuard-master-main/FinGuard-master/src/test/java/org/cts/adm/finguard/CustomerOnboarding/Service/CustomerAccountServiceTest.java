package org.cts.adm.finguard.CustomerOnboarding.Service;

import org.cts.adm.finguard.CustomerOnboarding.Eunm.AccountStatus;
import org.cts.adm.finguard.CustomerOnboarding.Eunm.KycStatus;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerAccountServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerAccountService customerAccountService;

    @Test
    void updateAccountStatus_activateVerifiedCustomer_success() {
        Customer customer = new Customer();
        customer.setCustomerId(10L);
        customer.setKycStatus(KycStatus.VERIFIED);
        customer.setAccountStatus(AccountStatus.PENDING);

        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));

        AccountStatus updated = customerAccountService.updateAccountStatus(10L, AccountStatus.ACTIVE);

        assertEquals(AccountStatus.ACTIVE, updated);
        assertEquals(AccountStatus.ACTIVE, customer.getAccountStatus());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void updateAccountStatus_activateWithoutVerifiedKyc_throwsBadRequest() {
        Customer customer = new Customer();
        customer.setCustomerId(11L);
        customer.setKycStatus(KycStatus.IN_PROGRESS);
        customer.setAccountStatus(AccountStatus.PENDING);

        when(customerRepository.findById(11L)).thenReturn(Optional.of(customer));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> customerAccountService.updateAccountStatus(11L, AccountStatus.ACTIVE));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void updateAccountStatus_reactivateClosedAccount_throwsBadRequest() {
        Customer customer = new Customer();
        customer.setCustomerId(12L);
        customer.setKycStatus(KycStatus.VERIFIED);
        customer.setAccountStatus(AccountStatus.CLOSED);

        when(customerRepository.findById(12L)).thenReturn(Optional.of(customer));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> customerAccountService.updateAccountStatus(12L, AccountStatus.ACTIVE));

        assertEquals(400, ex.getStatusCode().value());
    }
}

