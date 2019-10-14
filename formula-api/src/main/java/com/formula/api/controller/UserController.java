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
import com.formula.api.model.User;
import com.formula.api.repository.UserRepository;

@RestController
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@GetMapping("/user/{id}")
	public ResponseEntity<Object> getUserById(@PathVariable(value = "id") int userId) throws UserNotFoundException {
		Map<String, Object> body = new HashMap<String, Object>();
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

		body.put("status", HttpStatus.OK.value());
		body.put("user", user);
		return new ResponseEntity<Object>(body, HttpStatus.OK);
	}
	
	@GetMapping("/user/all")
	public ResponseEntity<Object> getAllUsers() {
		Map<String, Object> body = new HashMap<String, Object>();
		List<User> users = userRepository.findAll();

		body.put("status", HttpStatus.OK.value());
		body.put("users", users);
		return new ResponseEntity<Object>(body, HttpStatus.OK);
	}

	@PostMapping("/user")
	public ResponseEntity<Object> createUser(@Valid @RequestBody User user) {
		Map<String, Object> body = new HashMap<String, Object>();
		User newUser = userRepository.save(user);

		body.put("status", HttpStatus.OK.value());
		body.put("user", newUser);
		return new ResponseEntity<Object>(body, HttpStatus.OK);
	}

	@PutMapping("/user/{id}")
	public ResponseEntity<Object> updateUser(@PathVariable(value = "id") int userId,
			@Valid @RequestBody User userDetails)
			throws UserNotFoundException {
		Map<String, Object> body = new HashMap<String, Object>();
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));
				
		user.setFirstName(userDetails.getFirstName());
		user.setLastName(userDetails.getLastName());
		user.setEmail(userDetails.getEmail());
		User updatedUser = userRepository.save(user);
		
		body.put("status", HttpStatus.OK.value());
		body.put("user", updatedUser);
		return new ResponseEntity<Object>(body, HttpStatus.OK);
	}

	@DeleteMapping("/user/{id}")
	public ResponseEntity<Object> deleteUser(@PathVariable(value = "id") int userId) throws UserNotFoundException {
		Map<String, Object> body = new HashMap<String, Object>();
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));

		user.setActive(false);
		user.setDeleted(true);
		User updatedUser = userRepository.save(user);

		body.put("status", HttpStatus.OK.value());
		body.put("user", updatedUser);
		return new ResponseEntity<Object>(body, HttpStatus.OK);
	}

}
