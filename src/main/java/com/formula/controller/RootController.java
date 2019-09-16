package com.formula.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.formula.model.User;

@RestController
public class RootController {
	
	@RequestMapping("/")
	public String index() {
		return "Greetings from formula";
	}
}
