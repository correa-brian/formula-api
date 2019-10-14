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
import com.formula.api.model.User;
import com.formula.api.repository.UserRepository;

@SpringBootTest
public class UserControllerTest {
	@InjectMocks
	private UserController userController;

	@Mock
	private UserRepository userRepository;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetUserById() throws ResourceNotFoundException {
		User u = new User();
		u.setId(1);
		when(userRepository.findById(1)).thenReturn(Optional.of(u));

		ResponseEntity<HashMap<String, Object>> response = userController.getUserById(1);
		HashMap<String, Object> body = response.getBody();
		User user = (User) body.get("user");

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertTrue(body.get("user").equals(u));
		assertEquals(user.getId(), 1);
		assertTrue(user.isActive());
		assertFalse(user.isDeleted());
		assertEquals(body.get("status"), HttpStatus.OK.value());
	}

	@Test
	public void testGetAllUsers() throws ResourceNotFoundException {
		List<User> u = new ArrayList<User>();
		when(userRepository.findAll()).thenReturn(u);

		ResponseEntity<HashMap<String, Object>> response = userController.getAllUsers();
		HashMap<String, Object> body = response.getBody();

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertTrue(body.get("users").equals(u));
		assertEquals(body.get("status"), HttpStatus.OK.value());
	}

	@Test
	public void testCreateUser() {
		User u = new User();
		u.setEmail("sample@gmail.com");
		u.setFirstName("test");
		u.setLastName("name");

		when(userRepository.save(u)).thenReturn(u);
		ResponseEntity<HashMap<String, Object>> response = userController.createUser(u);
		HashMap<String, Object> body = response.getBody();
		User user = (User) body.get("user");

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertTrue(body.get("user").equals(u));
		assertEquals(user.getFirstName(), "test");
		assertEquals(user.getLastName(), "name");
		assertEquals(user.getEmail(), "sample@gmail.com");
		assertEquals(body.get("status"), HttpStatus.OK.value());
	}

	@Test
	public void testUpdateUser() throws ResourceNotFoundException {
		User u = new User();
		u.setId(2);
		u.setEmail("f@gmail.com");
		u.setFirstName("faux");
		u.setLastName("name");

		User updatedUser = new User();
		updatedUser.setId(2);
		updatedUser.setEmail("new email");
		updatedUser.setFirstName("new first name");
		updatedUser.setLastName("new last name");

		when(userRepository.findById(2)).thenReturn(Optional.of(u));
		when(userRepository.save(u)).thenReturn(u);

		ResponseEntity<HashMap<String, Object>> response = userController.updateUser(2, updatedUser);
		HashMap<String, Object> body = response.getBody();
		User user = (User) body.get("user");

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertTrue(body.get("user").equals(user));
		assertEquals(user.getId(), 2);
		assertTrue(user.isActive());
		assertFalse(user.isDeleted());
		assertEquals(user.getEmail(), "new email");
		assertEquals(body.get("status"), HttpStatus.OK.value());
	}

	@Test
	public void testDeleteUser() throws ResourceNotFoundException {
		User u = new User();
		u.setId(2);
		u.setEmail("f@gmail.com");
		u.setFirstName("faux");
		u.setLastName("name");

		when(userRepository.findById(2)).thenReturn(Optional.of(u));
		when(userRepository.save(u)).thenReturn(u);

		ResponseEntity<HashMap<String, Object>> response = userController.deleteUser(2);
		HashMap<String, Object> body = response.getBody();
		User user = (User) body.get("user");

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertTrue(user.isDeleted());
		assertFalse(user.isActive());
	}

}
