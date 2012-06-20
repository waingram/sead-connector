package edu.uiuc.ideals.sead;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.atom.abdera.ContentHelper;
import org.apache.abdera.model.Entry;
import org.dspace.content.Collection;
import org.dspace.eperson.EPerson;

/**
 * <p>Resource class to manage an individual collection.</p>
 */
public class CollectionResource extends BaseResource {

    private int collID;


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
                              int collID) {
        this.uriInfo = uriInfo;
        this.contentHelper = contentHelper;
        this.context = context;
        this.ePerson = ePerson;
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
}

