package com.formula.api.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.formula.api.exception.ResourceNotFoundException;
import com.formula.api.model.Customer;
import com.formula.api.repository.CustomerRepository;

@SpringBootTest
public class CustomerControllerTest {
	@InjectMocks
	private CustomerController customerController;

	@Mock
	CustomerRepository customerRepository;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetCustomerById() throws ResourceNotFoundException {
		Customer c = new Customer();
		c.setId(1);
		when(customerRepository.findById(1)).thenReturn(Optional.of(c));

		ResponseEntity<Object> customer = customerController.getCustomerById(1);
		HashMap body = (HashMap) customer.getBody();
		assertEquals(customer.getStatusCode(), HttpStatus.OK);
		assertTrue(body.get("customer").equals(c));
		assertEquals(body.get("status"), HttpStatus.OK.value());
	}

}
