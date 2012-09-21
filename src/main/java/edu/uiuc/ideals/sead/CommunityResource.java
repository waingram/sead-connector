package edu.uiuc.ideals.sead;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.atom.abdera.ContentHelper;
import org.apache.abdera.model.Entry;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Community;
import org.dspace.eperson.EPerson;

/**
 * <p>Resource class to manage an individual community.</p>
 */
public class CommunityResource extends BaseResource {

    private int communityID;


    /**
     * <p>Construct a configured instance of this resource class.</p>
     *
     * @param context
     * @param ePerson
     * @param communityID
     */
    public CommunityResource(UriInfo uriInfo,
                             ContentHelper contentHelper,
                             org.dspace.core.Context context,
                             EPerson ePerson,
                             int communityID) {
        this.uriInfo = uriInfo;
        this.contentHelper = contentHelper;
        this.context = context;
        this.ePerson = ePerson;
        this.communityID = communityID;
    }


    /**
     * <p>Return the community.</p>
     */
    @RolesAllowed("user")
    @GET
    @Produces({"application/atom+xml",
            "application/atom+xml;type=entry",
            "application/atom+json",
            "application/atom+json;type=entry",
            "application/json",
            "application/xml",
            "text/xml"})
    public Response get() {
        List<Community> communityList = findAuthorizedCommunities(null);
        if (communityList == null) {
            return Response.
                    status(Response.Status.NO_CONTENT).
                    type("text/plain").
                    entity("No authorized communities for ID '" + communityID + "'").
                    build();
        }
        for (Community community : communityList) {
            if (communityID == community.getID()) {
                Entry entry = null;
                try {
                    entry = communityAsEntry(community);
                } catch (SQLException e) {
                    log.error(e);
                    return Response.
                            status(Response.Status.INTERNAL_SERVER_ERROR).
                            type("text/plain").
                            entity(e.getMessage()).
                            build();
                }
                String uri = uriInfo.getRequestUriBuilder().build().toString();
                entry.addLink(uri, "self");
                entry.addLink(uri, "edit");
                return Response.ok(entry).build();
            }
        }
        return Response.
                status(Response.Status.NOT_FOUND).
                type("text/plain").
                entity("No community for ID '" + communityID + "'").
                build();
    }
    
    /**
     * <p>Create a new community based on the specified information.</p>
     */
    @RolesAllowed("user")
    @POST
    @Consumes({"application/atom+xml",
            "application/atom+xml;type=entry",
            "application/xml",
            "text/xml"})    
    public Response post(Entry entry) {
    	// Validate the incoming user information independent of the database
    	int parentCommunityID = communityID;
    	Community community = null;
    	entry.addExtension(DCTERMS_ISPARTOF).setText(String.valueOf(parentCommunityID));
        try {
        	community = communityFromEntry(entry);
            context.complete();
        } catch (SQLException e) {
            log.error(e);
            context.abort();
            return Response.
                    status(Response.Status.INTERNAL_SERVER_ERROR).
                    type("text/plain").
                    entity(e.getMessage()).
                    build();
        } catch (AuthorizeException e) {
            log.error(e);
            context.abort();
            return Response.
                    status(Response.Status.UNAUTHORIZED).
                    type("text/plain").
                    entity(e.getStackTrace()).
                    build();
        } catch (IOException e) {
            log.error(e);
            context.abort();
            return Response.
                    status(Response.Status.INTERNAL_SERVER_ERROR).
                    type("text/plain").
                    entity(e.getMessage()).
                    build();
        }
        return Response.
                    created(uriInfo.getRequestUriBuilder().path(community.getHandle()).build()).
                    build();

    }
    
    
    /**
     * <p>Update an existing community based on the specified information.</p>
     */
    @RolesAllowed("user")
    @PUT
    @Consumes({"application/atom+xml",
            "application/atom+xml;type=entry",
            "application/xml",
            "text/xml"})    
    public Response put(Entry entry) {
    	
        // Validate the incoming user information independent of the database
    	Community community = null;
    	entry.addExtension(DC_IDENTIFIER).setText(String.valueOf(communityID));
        try {
        	community = communityFromEntry(entry);
            context.complete();
        } catch (SQLException e) {
            log.error(e);
            context.abort();
            return Response.
                    status(Response.Status.INTERNAL_SERVER_ERROR).
                    type("text/plain").
                    entity(e.getMessage()).
                    build();
        } catch (AuthorizeException e) {
            log.error(e);
            context.abort();
            return Response.
                    status(Response.Status.UNAUTHORIZED).
                    type("text/plain").
                    entity(e.getStackTrace()).
                    build();
        } catch (IOException e) {
            log.error(e);
            context.abort();
            return Response.
                    status(Response.Status.INTERNAL_SERVER_ERROR).
                    type("text/plain").
                    entity(e.getMessage()).
                    build();
        }
        return Response.
                    created(uriInfo.getRequestUriBuilder().path(community.getHandle()).build()).
                    build();

    }
    
    
    /**
     * <p>Delete an existing community based on the specified information.</p>
     */
    @RolesAllowed("user")
    @DELETE    
    public Response delete() {
    	
    	// Validate the incoming user information independent of the database
    	Community community = null;
    	
        try {
        	community= Community.find(context, communityID);
        	community.delete();
            context.complete();
        } catch (SQLException e) {
            log.error(e);
            context.abort();
            return Response.
                    status(Response.Status.INTERNAL_SERVER_ERROR).
                    type("text/plain").
                    entity(e.getMessage()).
                    build();
        } catch (AuthorizeException e) {
            log.error(e);
            context.abort();
            return Response.
                    status(Response.Status.UNAUTHORIZED).
                    type("text/plain").
                    entity(e.getStackTrace()).
                    build();
        } catch (IOException e) {
            log.error(e);
            context.abort();
            return Response.
                    status(Response.Status.INTERNAL_SERVER_ERROR).
                    type("text/plain").
                    entity(e.getMessage()).
                    build();
        }
        return Response.
                    created(uriInfo.getRequestUriBuilder().path(community.getHandle()).build()).
                    build();

    }
}

