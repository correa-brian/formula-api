package com.formula.api.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class UserTest {
	private static User user;

	@Before
	public void setup() {
		user = new User();
	}

	@Test
	public void testGetFirstName() {
		user.setFirstName("brian");
		assertEquals(user.getFirstName(), "brian");
	}

	@Test
	public void testGetLasttName() {
		user.setLastName("formula");
		assertEquals(user.getLastName(), "formula");
	}

	@Test
	public void testGetEmail() {
		user.setEmail("brian@gmail.com");
		assertEquals(user.getEmail(), "brian@gmail.com");
	}

	@Test
	public void testGetCreatedAt() {
		assertNull(user.getCreatedAt());
	}

	@Test
	public void testGetUpdatedAt() {
		assertNull(user.getUpdatedAt());
	}

	@Test
	public void testSetActive() {
		user.setActive(false);
		assertFalse(user.isActive());
	}

	@Test
	public void testIsActive() {
		assertTrue(user.isActive());
	}

	@Test
	public void testIsDeleted() {
		assertFalse(user.isDeleted());
	}

	@Test
	public void testSetDeleted() {
		user.setDeleted(true);
		assertTrue(user.isDeleted());
	}

}
