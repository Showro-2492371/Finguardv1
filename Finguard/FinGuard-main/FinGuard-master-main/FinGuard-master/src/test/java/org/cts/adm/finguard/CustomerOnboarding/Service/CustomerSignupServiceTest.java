package org.cts.adm.finguard.CustomerOnboarding.Service;

import org.junit.jupiter.api.Test;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CustomerSignupServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerSignupService customerSignupService;

    @Test
    void signUp_success() {

        Customer customer = new Customer();
        customer.setName("John");
        customer.setContactInfo("john@test.com");
        customer.setPassword("password123");
        customer.setMfaEnabled(false);

        Mockito.when(passwordEncoder.encode("password123")).thenReturn("hashed-password");

        customerSignupService.SignUp(customer);

        Mockito.verify(customerRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void signUp_failure() {

        Customer customer = new Customer();
        customer.setName("John");
        customer.setContactInfo("john@test.com");
        customer.setPassword("password123");
        customer.setMfaEnabled(false);

        Mockito.when(passwordEncoder.encode("password123")).thenReturn("hashed-password");

        Mockito.when(customerRepository.save(Mockito.any()))
                .thenThrow(new RuntimeException("DB Error"));

        assertThrows(RuntimeException.class,
                () -> customerSignupService.SignUp(customer));
    }
}