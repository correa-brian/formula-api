package com.formula.api.controller;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PlaidControllerTest {
	@InjectMocks
	private PlaidController plaidController;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

}
