package pt.upa.transporter.ws.cli;

/**
 * This exception is used by the TransporterClient to throw exceptions with 
 * more meaningful error messages.
 */
public class TransporterClientException extends Exception {

	private static final long serialVersionUID = 1L;

	public TransporterClientException() {
	}

	public TransporterClientException(String message) {
		super(message);
	}

	public TransporterClientException(Throwable cause) {
		super(cause);
	}

	public TransporterClientException(String message, Throwable cause) {
		super(message, cause);
	}

}

