package org.cts.adm.finguard.CustomerOnboarding.Service;

import org.cts.adm.finguard.CustomerOnboarding.Eunm.AccountStatus;
import org.cts.adm.finguard.CustomerOnboarding.Eunm.Role;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.cts.adm.finguard.Jwt.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerLoginServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerLoginService customerLoginService;

    @Test
    void login_closedUser_throwsForbidden() {
        Customer customer = new Customer();
        customer.setCustomerId(15L);
        customer.setName("sam");
        customer.setPassword("secret");
        customer.setRole(Role.ROLE_USER);
        customer.setAccountStatus(AccountStatus.CLOSED);

        when(customerRepository.findAllByName("sam")).thenReturn(List.of(customer));
        when(passwordEncoder.encode("secret")).thenReturn("hashed-secret");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> customerLoginService.login("sam", "secret"));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void login_activeUser_returnsToken() {
        Customer customer = new Customer();
        customer.setCustomerId(16L);
        customer.setName("jane");
        customer.setPassword("secret");
        customer.setRole(Role.ROLE_USER);
        customer.setAccountStatus(AccountStatus.ACTIVE);

        when(customerRepository.findAllByName("jane")).thenReturn(List.of(customer));
        when(jwtUtil.generateToken(16L, "jane", "ROLE_USER")).thenReturn("token-123");
        when(passwordEncoder.encode("secret")).thenReturn("hashed-secret");

        String token = customerLoginService.login("jane", "secret");

        assertEquals("token-123", token);
    }

    @Test
    void login_pendingUser_returnsToken() {
        Customer customer = new Customer();
        customer.setCustomerId(15L);
        customer.setName("sam");
        customer.setPassword("secret");
        customer.setRole(Role.ROLE_USER);
        customer.setAccountStatus(AccountStatus.PENDING);

        when(customerRepository.findAllByName("sam")).thenReturn(List.of(customer));
        when(jwtUtil.generateToken(15L, "sam", "ROLE_USER")).thenReturn("token-pending");
        when(passwordEncoder.encode("secret")).thenReturn("hashed-secret");

        String token = customerLoginService.login("sam", "secret");

        assertEquals("token-pending", token);
    }

    @Test
    void login_suspendedUser_throwsForbidden() {
        Customer customer = new Customer();
        customer.setCustomerId(17L);
        customer.setName("blocked");
        customer.setPassword("secret");
        customer.setRole(Role.ROLE_USER);
        customer.setAccountStatus(AccountStatus.SUSPENDED);

        when(customerRepository.findAllByName("blocked")).thenReturn(List.of(customer));
        when(passwordEncoder.encode("secret")).thenReturn("hashed-secret");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> customerLoginService.login("blocked", "secret"));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void login_nullAccountStatus_defaultsToPendingAndReturnsToken() {
        Customer customer = new Customer();
        customer.setCustomerId(18L);
        customer.setName("legacy-user");
        customer.setPassword("secret");
        customer.setRole(Role.ROLE_USER);
        customer.setAccountStatus(null);

        when(customerRepository.findAllByName("legacy-user")).thenReturn(List.of(customer));
        when(jwtUtil.generateToken(18L, "legacy-user", "ROLE_USER")).thenReturn("token-legacy");
        when(passwordEncoder.encode("secret")).thenReturn("hashed-secret");

        String token = customerLoginService.login("legacy-user", "secret");

        assertEquals("token-legacy", token);
    }
}
