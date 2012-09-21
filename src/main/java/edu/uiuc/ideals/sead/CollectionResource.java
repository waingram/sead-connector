package edu.uiuc.ideals.sead;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.atom.abdera.ContentHelper;
import org.apache.abdera.model.Entry;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.eperson.EPerson;

/**
 * <p>Resource class to manage an individual collection.</p>
 */
public class CollectionResource extends BaseResource {

    private int collID;
    private int communityID;


    /**
     * <p>Construct a configured instance of this resource class.</p>
     *
     * @param context
     * @param ePerson
     * @param collID
     */
    public CollectionResource(UriInfo uriInfo,
                              ContentHelper contentHelper,
                              org.dspace.core.Context context,
                              EPerson ePerson,
                              int communityID, int collID) {
        this.uriInfo = uriInfo;
        this.contentHelper = contentHelper;
        this.context = context;
        this.ePerson = ePerson;
        this.communityID = communityID;
        this.collID = collID;
    }


    /**
     * <p>Return the contact information for the specified contact.</p>
     */
    @RolesAllowed("collID")
    @GET
    @Produces({"application/atom+xml",
            "application/atom+xml;type=entry",
            "application/atom+json",
            "application/atom+json;type=entry",
            "application/json",
            "application/xml",
            "text/xml"})
    public Response get() {
        List<Collection> collectionList = findAuthorizedCollections(null);
        if (collectionList == null) {
            return Response.
                    status(Response.Status.NO_CONTENT).
                    type("text/plain").
                    entity("No authorized collections for ID '" + collID + "'").
                    build();
        }
        for (Collection collection : collectionList) {
            if (collID == collection.getID()) {
                Entry entry = collectionAsEntry(collection);
                String uri = uriInfo.getRequestUriBuilder().build().toString();
                entry.addLink(uri, "self");
                entry.addLink(uri, "edit");
                return Response.ok(entry).build();
            }
        }
        return Response.
                status(Response.Status.NOT_FOUND).
                type("text/plain").
                entity("No collection for ID '" + collID + "'").
                build();
    }
    
    /**
     * <p>Update an existing collection based on the specified information.</p>
     */
    @RolesAllowed("user")
    @PUT
    @Consumes({"application/atom+xml",
            "application/atom+xml;type=entry",
            "application/xml",
            "text/xml"})    
    public Response put(Entry entry) {
    	
        // Validate the incoming user information independent of the database
    	Collection collection = null;
    	entry.addExtension(DC_IDENTIFIER).setText(String.valueOf(collID));
        try {
        	collection = collectionFromEntry(entry);
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
                    created(uriInfo.getRequestUriBuilder().path(collection.getHandle()).build()).
                    build();

    }
    
    
    /**
     * <p>Delete an existing collection based on the specified information.</p>
     */
    @RolesAllowed("user")
    @DELETE    
    public Response delete() {
    	
    	// Validate the incoming user information independent of the database

    	Community community = null;
    	Collection collection = null;
        try {
        	community = Community.find(context, communityID);
        	collection= Collection.find(context, collID);
        	community.removeCollection(collection);
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

