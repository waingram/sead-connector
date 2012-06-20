package edu.uiuc.ideals.sead.auth;

/**
 * A runtime exception representing a failure to provide correct
 * authentication credentials.
 *
 * @author Bill Ingram
 * @version %I% %G%
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message, String realm) {
        super(message);
        this.realm = realm;
    }

    private String realm = null;

    public String getRealm() {
        return this.realm;
    }
}
