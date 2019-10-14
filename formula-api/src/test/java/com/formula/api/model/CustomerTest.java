package com.formula.api.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CustomerTest {
	private static Customer customer;

	@Before
	public void setup() {
		customer = new Customer();
	}

	@Test
	public void testGetId() {
		customer.setId(1);
		assertEquals(customer.getId(), 1);
	}

	@Test
	public void testGetFirstName() {
		customer.setFirstName("formula");
		assertEquals(customer.getFirstName(), "formula");
	}

	@Test
	public void testGetLasttName() {
		customer.setLastName("eng");
		assertEquals(customer.getLastName(), "eng");
	}

	@Test
	public void testGetEmail() {
		customer.setEmail("test@formula.com");
		assertEquals(customer.getEmail(), "test@formula.com");
	}

	@Test
	public void testGetPhone() {
		customer.setPhone("555 - 5555");
		assertEquals(customer.getPhone(), "555 - 5555");
	}

	@Test
	public void testSetActive() {
		customer.setActive(false);
		assertFalse(customer.isActive());
	}

	@Test
	public void testIsActive() {
		assertTrue(customer.isActive());
	}

	@Test
	public void testSetDeleted() {
		customer.setDeleted(true);
		assertTrue(customer.isDeleted());
	}

	@Test
	public void testIsDeleted() {
		assertFalse(customer.isDeleted());
	}

	@Test
	public void testGetCreatedAt() {
		assertNull(customer.getCreatedAt());
	}

	@Test
	public void testGetUpdatedAt() {
		assertNull(customer.getUpdatedAt());
	}

}
