package com.formula.api.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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
	public User getNoteById(@PathVariable(value = "id") int userId) throws UserNotFoundException {
		return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
	}
	
	@GetMapping(path = "/user/all")
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	@PostMapping("/user")
	public User createUser(@Valid @RequestBody User user) {
		return userRepository.save(user);
	}

	@PutMapping("/user/{id}")
	public User updateUser(@PathVariable(value = "id") int userId, @Valid @RequestBody User userDetails)
			throws UserNotFoundException {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));
				
		user.setFirstName(userDetails.getFirstName());
		user.setLastName(userDetails.getLastName());
		user.setEmail(userDetails.getEmail());
		
		User updatedUser = userRepository.save(user);
		return updatedUser;
	}

	@DeleteMapping("/user/{id}")
	public User deleteUser(@PathVariable(value = "id") int userId) throws UserNotFoundException {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));

		user.setActive(false);
		user.setDeleted(true);
		return userRepository.save(user);
	}

}
