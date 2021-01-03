package de.tum.ei.lkn.eces.network.exceptions;

/**
 * Exception thrown when wrong Network operations are performed.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class NetworkException extends RuntimeException {
	private static final long serialVersionUID = 3313533029680219602L;

	public NetworkException(String string) {
		super(string);
	}
}