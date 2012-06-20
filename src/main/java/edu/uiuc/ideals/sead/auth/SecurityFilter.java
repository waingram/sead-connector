package edu.uiuc.ideals.sead.auth;

import java.security.Principal;
import java.sql.SQLException;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import org.apache.log4j.Logger;
import org.dspace.authenticate.AuthenticationManager;
import org.dspace.authenticate.AuthenticationMethod;
import org.dspace.eperson.EPerson;

/**
 * @author Bill Ingram
 * @version %I% %G%
 * @see com.sun.jersey.spi.container.ContainerRequest
 */
public class SecurityFilter implements ContainerRequestFilter {

    private static Logger log = Logger.getLogger(SecurityFilter.class);

    @Context
    UriInfo uriInfo;

    /**
     * The realm name to use in authentication challenges.
     */
    private static final String REALM = "SEAD";

    /**
     * Authenticate the user for this request, and add a security context
     * so that role checking can be performed.
     *
     * @param containerRequest The request we re processing
     * @return the decorated request
     * @exception edu.uiuc.ideals.sead.auth.AuthenticationException if authentication credentials
     *  are missing or invalid
     */
    @Override
    public ContainerRequest filter(ContainerRequest containerRequest) {
        EPerson ePerson = null;
        try {
            ePerson = authenticate(containerRequest);
        } catch (SQLException e) {
            log.error(e);
            e.printStackTrace();
            return null;
        }
        containerRequest.setSecurityContext(new Authorizer(ePerson));
        return containerRequest;
    }

    /**
     * Perform the required authentication checks, and return the
     * {@link EPerson} instance for the authenticated user.
     *
     * @throws MappableContainerException if authentication fails
     *                                    (will contain an AuthenticationException)
     */
    private EPerson authenticate(ContainerRequest request) throws SQLException {

        // Extract authentication credentials
        String authentication = request.getHeaderValue(ContainerRequest.AUTHORIZATION);
        if (authentication == null) {
            throw new MappableContainerException
                    (new AuthenticationException("Authentication credentials are required\r\n", REALM));
        }
        if (!authentication.startsWith("Basic ")) {
            throw new MappableContainerException
                    (new AuthenticationException("Only HTTP Basic authentication is supported\r\n", REALM));
        }
        authentication = authentication.substring("Basic ".length());
        String[] values = new String(Base64.base64Decode(authentication)).split(":");
        if (values.length < 2) {
            throw new MappableContainerException
                    (new AuthenticationException("Invalid syntax for username and password\r\n", REALM));
        }
        String username = values[0];
        String password = values[1];
        if ((username == null) || (password == null)) {
            throw new MappableContainerException
                    (new AuthenticationException("Missing username or password\r\n", REALM));
        }

        org.dspace.core.Context context = context = new org.dspace.core.Context();

        // Validate the extracted credentials
        EPerson ePerson = null;
        int auth = AuthenticationManager.authenticate(context, username, password, null, null);
        if (auth != AuthenticationMethod.SUCCESS) {
            throw new MappableContainerException(new AuthenticationException("Invalid username or password\r\n", REALM));
        } else {
            ePerson = context.getCurrentUser();
        }

        //Return the validated EPseron
        return ePerson;
    }

    /**
     * SecurityContext used to perform authorization checks.
     */
    public class Authorizer implements SecurityContext {

        private Principal principal = null;

        public Authorizer(final EPerson ePerson) {
            if (ePerson != null) {
                principal = new Principal() {
                    public String getName() {
                        return ePerson.getEmail();
                    }
                };
            }
        }

        @Override
        public Principal getUserPrincipal() {
            return principal;
        }

        /**
         * Determine whether the authenticated user possesses the requested
         * role.
         * <p/>
         * <p>This process could be doing much more with DSpace groups
         * for this simple case, though, all that is checked is whether
         * or not an EPerson exists, which, at this point, should always
         * be true.
         * </p>
         *
         * @param role Role to be checked
         */
        @Override
        public boolean isUserInRole(String role) {
            if ("admin".equals(role)) {
                return false;
            } else if ("user".equals(role)) {
                return principal != null;
            }
            return false;
        }

        @Override
        public boolean isSecure() {
            return "https".equals(uriInfo.getRequestUri().getScheme());
        }

        @Override
        public String getAuthenticationScheme() {
            if (principal == null) {
                return null;
            }
            return SecurityContext.BASIC_AUTH;
        }

    }
}
