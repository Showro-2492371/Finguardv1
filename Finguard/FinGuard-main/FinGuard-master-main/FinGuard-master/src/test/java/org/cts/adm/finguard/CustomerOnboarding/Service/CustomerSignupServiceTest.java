package org.cts.adm.finguard.CustomerOnboarding.Service;

import org.junit.jupiter.api.Test;
import org.cts.adm.finguard.CustomerOnboarding.Dto.CustomerSignupRequest;
import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CustomerSignupServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerSignupService customerSignupService;

    @Test
    void signUp_success() {

        CustomerSignupRequest request = new CustomerSignupRequest();
        request.setName("John");
        request.setContactInfo("john@test.com");
        request.setPassword("password123");
        request.setMfaEnabled(false);

        customerSignupService.signUp(request);

        Mockito.verify(customerRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void signUp_failure() {

        CustomerSignupRequest request = new CustomerSignupRequest();
        request.setName("John");
        request.setContactInfo("john@test.com");
        request.setPassword("password123");
        request.setMfaEnabled(false);

        Mockito.when(customerRepository.save(Mockito.any()))
                .thenThrow(new RuntimeException("DB Error"));

        assertThrows(RuntimeException.class,
                () -> customerSignupService.signUp(request));
    }
}