package edu.uiuc.ideals.sead;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.dspace.content.Community;
import org.dspace.core.ConfigurationManager;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Resource class hosted at the URI path "/communities"
 */
@Path("/communities")
public class CommunitiesResource extends BaseResource {

    public CommunitiesResource(@Context UriInfo uriInfo,
                               @Context SecurityContext securityContext,
                               @Context HttpServletRequest request) {
        this.uriInfo = uriInfo;
        this.context = createContext(request);
        this.ePerson = findEPerson(securityContext);
        this.context.setCurrentUser(ePerson);
    }

    /**
     * <p>Return a set of communities the user can access.</p>
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
    public Response get() {
    	List<Community> communityList = findAuthorizedCommunities(null);
        
    	Feed feed = abdera.newFeed();
        feed.setId("collections");
        feed.setTitle(ConfigurationManager.getProperty("dspace.name"));
        feed.setUpdated(new Date());
        feed.addLink(uriInfo.getRequestUriBuilder().build().toString(), "self");
        for (Community community : communityList) {
            Entry entry = null;
            try {
                entry = communityAsEntry(community);
            } catch (SQLException e) {
                log.error(e);
                context.abort();
                return Response.
                        status(Response.Status.INTERNAL_SERVER_ERROR).
                        type("text/plain").
                        entity(e.getMessage()).
                        build();
            }
            feed.addEntry(entry);
        }
        
        return Response.ok(feed).build();
    }    
    

    /**
     * <p>Return an instance of {@link CollectionResource} configured for the
     * specified username.</p>
     *
     * @param communityID ID of the specified community
     */
    @Path("{communityID}")
    public CommunityResource community(@PathParam("communityID") int communityID) {
    	return new CommunityResource(uriInfo, contentHelper, context, ePerson, communityID);

    }    
    

}

