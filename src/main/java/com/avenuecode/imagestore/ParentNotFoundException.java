package com.avenuecode.imagestore;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ParentNotFoundException extends RuntimeException {

	public ParentNotFoundException(Long productId) {
		super("The parentId '" + productId + "' does not refer to an existing product." );
	}

	private static final long serialVersionUID = 1L;

}
