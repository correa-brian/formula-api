package com.formula.api.exception;

public class ResourceNotFoundException extends Exception {

	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = -7901757429744812055L;

	public ResourceNotFoundException(int userId) {
		super(String.format("User with id: %s is not found", userId));
	}

}
