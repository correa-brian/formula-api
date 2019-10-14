package com.formula.api.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.formula.api.exception.ResourceNotFoundException;
import com.formula.api.model.User;
import com.formula.api.repository.UserRepository;

@RunWith(SpringRunner.class)
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

		ResponseEntity<Object> user = userController.getUserById(1);
		HashMap body = (HashMap) user.getBody();
		assertEquals(user.getStatusCode(), HttpStatus.OK);
		assertTrue(body.get("user").equals(u));
		assertEquals(body.get("status"), HttpStatus.OK.value());
	}

	@Test
	public void testGetAllUser() throws ResourceNotFoundException {
		List<User> u = new ArrayList<User>();
		when(userRepository.findAll()).thenReturn(u);

		// TODO: validate all user response.
	}

	// TODO: Test POST, PUT, DELETE methods

}
