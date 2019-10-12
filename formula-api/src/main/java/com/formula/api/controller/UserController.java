package com.formula.api.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.formula.api.model.User;
import com.formula.api.repository.UserRepository;

@RestController
@RequestMapping(path="/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@GetMapping("/{id}")
	public User getNoteById(@PathVariable(value = "id") int userId) throws Exception {
		return userRepository.findById(userId).orElseThrow(() -> new Exception());
	}
	
	@GetMapping(path="/all")
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	@PostMapping
	public User createUser(@Valid @RequestBody User user) {
		return userRepository.save(user);
	}

}
