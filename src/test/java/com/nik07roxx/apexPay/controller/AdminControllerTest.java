package com.nik07roxx.apexPay.controller;

import com.nik07roxx.apexPay.Controller.AdminController;
import com.nik07roxx.apexPay.Service.AdminService;
import com.nik07roxx.apexPay.Service.Implementation.JwtService;
import com.nik07roxx.apexPay.Service.Implementation.UserDetailsServiceImpl;
import com.nik07roxx.apexPay.exceptions.CustomerNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc; // This tool acts like a fake browser

    @MockBean
    private AdminService adminService; // Replaces the service with a mock

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void shouldReturnUserList() throws Exception {
        // Arrange: Mock the service behavior
        when(adminService.findAllUsers(any())).thenReturn(Page.empty());

        // Act & Assert: Simulate a real HTTP request
        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); // Verifies HTTP 200
    }

    @Test
    @DisplayName("Should return 404 Not Found when customer ID does not exist")
    void shouldReturn404WhenCustomerDoesNotExist() throws Exception {
        // Arrange: Tell Mockito to throw the exception when this ID is used
        when(adminService.blockCustomerAndAllAccounts(eq(999L), any()))
                .thenThrow(new CustomerNotFoundException("Customer not found"));

        // Act & Assert
        mockMvc.perform(patch("/api/admin/customer/999/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Test\"}"))
                .andDo(print())
                .andExpect(status().isNotFound()); // Expect 404
    }
}
