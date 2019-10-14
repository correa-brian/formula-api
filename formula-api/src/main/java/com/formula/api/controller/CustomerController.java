package com.formula.api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.formula.api.exception.UserNotFoundException;
import com.formula.api.model.Customer;
import com.formula.api.repository.CustomerRepository;

@RestController
public class CustomerController {

	@Autowired
	private CustomerRepository customerRepository;

	@GetMapping("/customer/{id}")
	public ResponseEntity<Object> getCustomerById(@PathVariable(value = "id") int customerId)
			throws UserNotFoundException {
		Map<String, Object> body = new HashMap<String, Object>();
		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new UserNotFoundException(customerId));

		body.put("status", HttpStatus.OK.value());
		body.put("customer", customer);
		return new ResponseEntity<Object>(body, HttpStatus.OK);
	}

	@GetMapping("/customer/all")
	public ResponseEntity<Object> getAllCustomers() {
		Map<String, Object> body = new HashMap<String, Object>();
		List<Customer> customers = customerRepository.findAll();

		body.put("status", HttpStatus.OK.value());
		body.put("customers", customers);
		return new ResponseEntity<Object>(body, HttpStatus.OK);
	}

	@PostMapping("/customer")
	public ResponseEntity<Object> createCustomer(@Valid @RequestBody Customer customer) {
		Map<String, Object> body = new HashMap<String, Object>();
		Customer newCustomer = customerRepository.save(customer);

		body.put("status", HttpStatus.OK.value());
		body.put("user", newCustomer);
		return new ResponseEntity<Object>(body, HttpStatus.OK);
	}

	@PutMapping("/customer/{id}")
	public ResponseEntity<Object> updateCustomer(@PathVariable(value = "id") int customerId,
			@Valid @RequestBody Customer customerDetails) throws UserNotFoundException {
		Map<String, Object> body = new HashMap<String, Object>();
		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new UserNotFoundException(customerId));

		customer.setFirstName(customerDetails.getFirstName());
		customer.setLastName(customerDetails.getLastName());
		customer.setEmail(customerDetails.getEmail());
		customer.setPhone(customerDetails.getPhone());
		Customer updatedCustomer = customerRepository.save(customer);

		body.put("status", HttpStatus.OK.value());
		body.put("customer", updatedCustomer);
		return new ResponseEntity<Object>(body, HttpStatus.OK);
	}

	@DeleteMapping("/customer/{id}")
	public ResponseEntity<Object> deleteCustomer(@PathVariable(value = "id") int customerId)
			throws UserNotFoundException {
		Map<String, Object> body = new HashMap<String, Object>();
		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new UserNotFoundException(customerId));

		customer.setActive(false);
		customer.setDeleted(true);
		Customer updatedCustomer = customerRepository.save(customer);

		body.put("status", HttpStatus.OK.value());
		body.put("user", updatedCustomer);
		return new ResponseEntity<Object>(body, HttpStatus.OK);
	}


}
