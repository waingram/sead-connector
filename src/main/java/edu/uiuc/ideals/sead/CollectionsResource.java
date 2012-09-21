package edu.uiuc.ideals.sead;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.core.ConfigurationManager;

/**
 * Resource class hosted at the URI path "/collections"
 */
@Path("/communities/{communityID}/collections")
public class CollectionsResource extends BaseResource {
	
	
    public CollectionsResource(@Context UriInfo uriInfo,
                               @Context SecurityContext securityContext,
                               @Context HttpServletRequest request) {
        this.uriInfo = uriInfo;
        this.context = createContext(request);
        this.ePerson = findEPerson(securityContext);
        this.context.setCurrentUser(ePerson);
    }

    /**
     * <p>Return a set of collections the user can access in given Community.</p>
     */
    @RolesAllowed("user")
    @GET
    @Produces({"application/atom+xml",
            "application/atom+xml;type=feed",
            "application/atom+json",
            "application/atom+json;type=feed",
            "application/json",
            "application/xml",
            "text/xml"})
    public Response get(@PathParam("communityID") int parentCommunityID) {
    	
    	Feed feed = abdera.newFeed();
        feed.setId("collections");
        feed.setTitle(ConfigurationManager.getProperty("dspace.name"));
        feed.setUpdated(new Date());
        feed.addLink(uriInfo.getRequestUriBuilder().build().toString(), "self");
    	
    	try {
    		
	        List<Collection> collectionList = findAuthorizedCollections(Community.find(context, parentCommunityID));
	        
	        for (Collection collection : collectionList) {
	        	Entry entry = abdera.newEntry();
			    entry.setId(String.valueOf(collection.getID()));
			    entry.setTitle(collection.getMetadata("name"));
			    entry.setRights(collection.getLicense());
			    entry.addLink(ConfigurationManager.getProperty("sword.deposit.url"));
			    //contentHelper.setContentEntity(entry, MediaType.APPLICATION_XML_TYPE, collection);
			    feed.addEntry(entry);				
	        }        	
	        
        } catch (SQLException e) {
        	log.error(e);
            context.abort();
            return Response.
                    status(Response.Status.INTERNAL_SERVER_ERROR).
                    type("text/plain").
                    entity(e.getMessage()).
                    build();
		}

        return Response.ok(feed).build();
    }

    /**
     * <p>Create a new collection based on the specified information.</p>
     */
    @RolesAllowed("user")
    @POST
    @Consumes({"application/atom+xml",
            "application/atom+xml;type=entry",
            "application/xml",
            "text/xml"})
    public Response post(Entry entry, @PathParam("communityID") int parentCommunityID) {
    	
    	entry.addExtension(DCTERMS_ISPARTOF).setText(String.valueOf(parentCommunityID));
        Collection collection = null;
        try {
        	System.out.println(entry);
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
        
        return Response.created(uriInfo.getRequestUriBuilder().path(String.valueOf(collection.getID())).build()).
                build();

    }


    /**
     * <p>Return an instance of {@link CollectionResource} configured for the
     * specified username.</p>
     *
     * @param collID ID of the specified collection
     */
    @Path("{collID}")
    public CollectionResource collection(@PathParam("communityID") int communityID, @PathParam("collID") int collID) {
        return new CollectionResource(uriInfo, contentHelper, context, ePerson, communityID, collID);

    }

}

