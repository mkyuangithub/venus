package com.mkyuan.fountaingateway.common.controller.exception;

public class LoginFailedException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public LoginFailedException(String message) {
		super(message);
	}
}
