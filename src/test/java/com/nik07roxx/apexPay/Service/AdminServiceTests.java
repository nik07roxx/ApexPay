package com.nik07roxx.apexPay.Service;
import com.nik07roxx.apexPay.DTO.Customer.CustomerBlockRequest;
import com.nik07roxx.apexPay.DTO.Customer.CustomerBlockResponse;
import com.nik07roxx.apexPay.Entity.Account;
import com.nik07roxx.apexPay.Entity.Customer;
import com.nik07roxx.apexPay.Entity.Role;
import com.nik07roxx.apexPay.Entity.User;
import com.nik07roxx.apexPay.Repository.CustomerRepository;
import com.nik07roxx.apexPay.Repository.RoleRepository;
import com.nik07roxx.apexPay.Repository.UserRepository;
import com.nik07roxx.apexPay.Service.Implementation.AdminServiceImpl;
import com.nik07roxx.apexPay.model.AccountStatus;
import com.nik07roxx.apexPay.model.CustomerStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTests {

    @InjectMocks
    private AdminServiceImpl adminService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Test
    void blockCustomerAndAllAccounts_ShouldBlockCustomerAndAllAccounts() {
        // 1. Arrange
        Long customerId = 10L;
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setStatus(CustomerStatus.ACTIVE);

        Account acc1 = new Account();
        acc1.setStatus(AccountStatus.ACTIVE);
        Account acc2 = new Account();
        acc2.setStatus(AccountStatus.ACTIVE);
        customer.setAccounts(List.of(acc1, acc2));

        CustomerBlockRequest request = new CustomerBlockRequest("Fraudulent activity");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // 2. Act
        // TODO: Call your service method here
        CustomerBlockResponse customerBlockResponse = adminService.blockCustomerAndAllAccounts(customerId, request);

        // 3. Assert
        // TODO: Verify customer status is BLOCKED
        assertEquals(CustomerStatus.BLOCKED,customer.getStatus());
        // TODO: Verify acc1 and acc2 status are BLOCKED
        assertEquals(AccountStatus.BLOCKED,acc1.getStatus());
        assertEquals(AccountStatus.BLOCKED,acc2.getStatus());
        // TODO: Verify customerRepository.save() was called
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    public void testpromoteUserToAdmin()
    {
        // Setup
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        Role role = new Role();
        role.setId(1L);
        role.setName("ADMIN");

        // when mocks
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // act
        adminService.promoteUserToAdmin(userId);

        // assert
        assertTrue(user.getRoles().contains(role));
        verify(userRepository, times(1)).save(any(User.class));
    }
}
