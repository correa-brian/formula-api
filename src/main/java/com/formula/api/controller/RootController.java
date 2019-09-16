package com.formula.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.formula.api.model.User;

@RestController
public class RootController {
	
	@RequestMapping("/")
	public String index() {
		return "Greetings from formula";
	}
}
