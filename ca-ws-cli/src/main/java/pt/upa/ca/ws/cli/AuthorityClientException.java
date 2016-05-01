package pt.upa.ca.ws.cli;

public class AuthorityClientException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AuthorityClientException() {
	}

	public AuthorityClientException(String message) {
		super(message);
	}

	public AuthorityClientException(Throwable cause) {
		super(cause);
	}

	public AuthorityClientException(String message, Throwable cause) {
		super(message, cause);
	}
}
