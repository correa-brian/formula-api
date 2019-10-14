package com.formula.api.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

		ResponseEntity<HashMap<String, Object>> response = customerController.getCustomerById(1);
		HashMap<String, Object> body = response.getBody();
		Customer customer = (Customer) body.get("customer");

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertTrue(body.get("customer").equals(c));
		assertEquals(customer.getId(), 1);
		assertTrue(customer.isActive());
		assertFalse(customer.isDeleted());
		assertEquals(body.get("status"), HttpStatus.OK.value());
	}

	@Test
	public void testGetAllCustomers() throws ResourceNotFoundException {
		List<Customer> c = new ArrayList<Customer>();
		when(customerRepository.findAll()).thenReturn(c);

		ResponseEntity<HashMap<String, Object>> response = customerController.getAllCustomers();
		HashMap<String, Object> body = response.getBody();

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertTrue(body.get("customers").equals(c));
		assertEquals(body.get("status"), HttpStatus.OK.value());
	}

	@Test
	public void testCreateCustomer() {
		Customer c = new Customer();
		c.setFirstName("test");
		c.setLastName("name");
		c.setEmail("email.com");

		when(customerRepository.save(c)).thenReturn(c);
		ResponseEntity<HashMap<String, Object>> response = customerController.createCustomer(c);
		HashMap<String, Object> body = response.getBody();
		Customer customer = (Customer) body.get("customer");

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertTrue(body.get("customer").equals(c));
		assertEquals(customer.getFirstName(), "test");
		assertEquals(customer.getLastName(), "name");
		assertEquals(customer.getEmail(), "email.com");
		assertEquals(body.get("status"), HttpStatus.OK.value());
	}

	@Test
	public void testUpdateCustomer() throws ResourceNotFoundException {
		Customer c = new Customer();
		c.setId(2);
		c.setEmail("f@gmail.com");
		c.setFirstName("faux");
		c.setLastName("name");

		Customer updatedCustomer = new Customer();
		updatedCustomer.setId(2);
		updatedCustomer.setEmail("new email");
		updatedCustomer.setFirstName("new first name");
		updatedCustomer.setLastName("new last name");

		when(customerRepository.findById(2)).thenReturn(Optional.of(c));
		when(customerRepository.save(c)).thenReturn(c);

		ResponseEntity<HashMap<String, Object>> response = customerController.updateCustomer(2, updatedCustomer);
		HashMap<String, Object> body = response.getBody();
		Customer customer = (Customer) body.get("customer");

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertTrue(body.get("customer").equals(customer));
		assertEquals(customer.getId(), 2);
		assertTrue(customer.isActive());
		assertFalse(customer.isDeleted());
		assertEquals(customer.getEmail(), "new email");
		assertEquals(body.get("status"), HttpStatus.OK.value());
	}

	@Test
	public void testDeleteCustomer() throws ResourceNotFoundException {
		Customer c = new Customer();
		c.setId(2);
		c.setEmail("f@gmail.com");
		c.setFirstName("faux");
		c.setLastName("name");

		when(customerRepository.findById(2)).thenReturn(Optional.of(c));
		when(customerRepository.save(c)).thenReturn(c);

		ResponseEntity<HashMap<String, Object>> response = customerController.deleteCustomer(2);
		HashMap<String, Object> body = response.getBody();
		Customer customer = (Customer) body.get("customer");

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertTrue(customer.isDeleted());
		assertFalse(customer.isActive());
	}
}
