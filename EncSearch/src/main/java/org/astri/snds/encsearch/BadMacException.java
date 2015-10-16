package org.astri.snds.encsearch;

import java.security.SignatureException;

public class BadMacException extends SignatureException {

	private static final long serialVersionUID = 1L;
	private String forWhat;

	public BadMacException() {}
	public BadMacException(String forWhat_) {
		forWhat = forWhat_;
	}
	
	public String toString() {
		return String.format("Invalid MAC detected for %s", forWhat);
	}
}
