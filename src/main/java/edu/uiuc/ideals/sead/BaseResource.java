package edu.uiuc.ideals.sead;

import java.io.IOException;
import java.security.Principal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.xml.namespace.QName;

import com.sun.jersey.atom.abdera.ContentHelper;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.core.Constants;
import org.dspace.eperson.EPerson;

import static org.dspace.authorize.AuthorizeManager.authorizeActionBoolean;
import static org.dspace.eperson.EPerson.findByEmail;


/**
 * <p>Abstract base class for resources.</p>
 */
public abstract class BaseResource {

    protected static Logger log = Logger.getLogger(BaseResource.class);

    /**
     * Base SEAD community
     */
    protected static final int BASE_COMMUNITY_ID = 1;


    /**
     * <p>Shared instance of <code>Abdera</code> factory object.</p>
     */
    protected static final Abdera abdera = Abdera.getInstance();

    /**
     * <p>Injected helper for manipulating the content element in an Atom entry.</p>
     */
    protected ContentHelper contentHelper = null;

    /**
     * <p>Injected information about the current request</p>
     */
    protected UriInfo uriInfo = null;

    /**
     * <p>DSpace {@link EPerson}</p>
     */
    protected EPerson ePerson;

    /**
     * <p>DSpace {@link org.dspace.core.Context}</p>
     */
    protected org.dspace.core.Context context;

    /**
     * <p>Find a DSpace {@link EPerson}.</p>
     *
     * @param securityContext {@link SecurityContext}
     * @return {@link EPerson}
     */
    protected EPerson findEPerson(SecurityContext securityContext) {

        Principal p = securityContext.getUserPrincipal();
        String email = p != null ? p.getName() : "";
        try {
            return findByEmail(context, email);
        } catch (SQLException e) {
            log.error(e);
            return null;
        } catch (AuthorizeException e) {
            log.error(e);
            return null;
        }
    }

    /**
     * <p>Create a DSpace {@link org.dspace.core.Context}.</p>
     *
     * @param request {@link HttpServletRequest}
     * @return {@link org.dspace.core.Context}
     */
    protected org.dspace.core.Context createContext(HttpServletRequest request) {

        try {
            context = new org.dspace.core.Context();
        } catch (SQLException e) {
            log.error(e);
            return null;
        }

        String ip = request.getRemoteAddr();

        //Set the session ID and IP address
        context.setExtraLogInfo("session_id=0:ip_addr=" + ip);
        return context;
    }

    /**
     * <p>Return an array of authorized communities.</p>
     *
     * @param parent parent {@link Community}
     * @return ArrayList of {@link Community} with matching permissions
     */
    protected List<Community> findAuthorizedCommunities(Community parent) {
        List<Community> communities = new ArrayList<Community>();

        try {
            Community[] allCommunities = parent != null ? parent.getSubcommunities() : Community.findAll(context);

            for (Community community : allCommunities) {
                if (authorizeActionBoolean(context, community, Constants.ADD, true)) {
                    communities.add(community);
                }
            }
        } catch (SQLException e) {
            log.error(e);
            return null;
        }
        return communities;
    }

    /**
     * <p>Return an array of authorized collections.</p>
     *
     * @param community parent {@link Community}
     * @return ArrayList of {@link Collection} with matching permissions
     */
    protected List<Collection> findAuthorizedCollections(Community community) {
        List<Collection> collections = new ArrayList<Collection>();

        try {
            Collection[] allCollections = community != null ? community.getCollections() : Collection.findAll(context);

            for (Collection collection : allCollections) {
                if (authorizeActionBoolean(context, collection, Constants.ADD, true)) {
                    collections.add(collection);
                }
            }
        } catch (SQLException e) {
            log.error(e);
            return null;
        }
        return collections;
    }

    /**
     * Define metadata namespaces and QNAMEs
     */
    private final String DC = "http://purl.org/dc/elements/1.1/";
    private final String DCTERMS = "http://purl.org/dc/terms/";
    private final String DCMITYPE = "http://purl.org/dc/dcmitype/";
    private final String MARCREL = "http://www.loc.gov/loc.terms/relators/";
    private final String XSI = "http://www.w3.org/2001/XMLSchema-instance";

    private final QName DC_TYPE = new QName(DC, "type", "dc");
    protected final QName DC_IDENTIFIER = new QName(DC, "identifier", "dc");
    private final QName DC_TITLE = new QName(DC, "title", "dc");
    private final QName DC_RIGHTS = new QName(DC, "rights", "dc");
    protected final QName DCTERMS_ISPARTOF = new QName(DCTERMS, "isPartOf", "dcterms");
    private final QName DCTERMS_ALTERNATIVE = new QName(DCTERMS, "alternative", "dcterms");
    private final QName DCTERMS_ABSTRACT = new QName(DCTERMS, "abstract", "dcterms");
    private final QName DCTERMS_CONFORMSTO = new QName(DCTERMS, "conformsTo", "dcterms");
    private final QName DCTERMS_PROVENANCE = new QName(DCTERMS, "provenance", "dcterms");
    private final QName XSI_TYPE = new QName(XSI, "type", "xsi");


    /**
     * <p>Return a new {@link Community} created from the contents of the specified {@link Entry}.</p>
     *
     * @param entry Atom {@link Entry} containing the details
     */
    protected Community communityFromEntry(Entry entry) throws SQLException, AuthorizeException, IOException {

        String parentId = entry.getExtension(DCTERMS_ISPARTOF) != null?entry.getExtension(DCTERMS_ISPARTOF).getText(): null;
        String communityId = entry.getExtension(DC_IDENTIFIER) != null?entry.getExtension(DC_IDENTIFIER).getText(): null;
        Community parentCommunity = null;

        if (StringUtils.isNotBlank(parentId)) {
            parentCommunity = Community.find(context, Integer.parseInt(parentId));
        }

        Community community;
        if (parentCommunity != null)
            community = parentCommunity.createSubcommunity();
        else
            community = Community.find(context, Integer.parseInt(communityId));

        String name = entry.getExtension(DC_TITLE).getText();
        String copyrightText = entry.getExtension(DC_RIGHTS).getText();
        String shortDescription = entry.getExtension(DCTERMS_ALTERNATIVE).getText();
        String introductoryText = entry.getExtension(DCTERMS_ABSTRACT).getText();

        // If they don't have a name then make it untitled.
        if (StringUtils.isBlank(name)) name = "Untitled";

        // If empty, make it null.
        // @see org.dspace.app.xmlui.aspect.administrative.FlowContainerUtils#processCreateCommunity
        if (StringUtils.isBlank(shortDescription)) shortDescription = null;
        if (StringUtils.isBlank(introductoryText)) introductoryText = null;
        if (StringUtils.isBlank(copyrightText)) copyrightText = null;

        community.setMetadata("name", name);
        community.setMetadata("copyright_text", copyrightText);
        community.setMetadata("short_description", shortDescription);
        community.setMetadata("introductory_text", introductoryText);

        community.update();
        context.commit();

        return community;
    }

    /**
     * <p>Return this {@link Community} as an Atom {@link Entry} suitable for
     * inclusion in a web service request or response.  Note that no Atom
     * links will have been defined, because they depend upon the server
     * environment in which these instances are embedded.</p>
     */
    protected Entry communityAsEntry(Community community) throws SQLException {
        Entry entry = abdera.newEntry();

        int id = community.getID();
        int parentId = -1;
        Community parent = community.getParentCommunity();
        if (parent != null) {
            parentId = parent.getID();
        }

        String name = community.getName();
        String handle = community.getHandle();
        String shortDescription = community.getMetadata("short_description");
        String introductoryText = community.getMetadata("introductory_text");
        String copyrightText = community.getMetadata("copyright_text");
        String sideBarText = community.getMetadata("side_bar_text");

        entry.addExtension(DC_TYPE).setText("Community");
        entry.addExtension(DC_TITLE).setText(name);
        entry.addExtension(DC_RIGHTS).setText(copyrightText);
        entry.addExtension(DCTERMS_ALTERNATIVE).setText(shortDescription);
        entry.addExtension(DCTERMS_ABSTRACT).setText(introductoryText);
        entry.addExtension(DC_IDENTIFIER).setText(String.valueOf(id));
        entry.addExtension(DCTERMS_ISPARTOF).setText(String.valueOf(parentId));

        Element doi = entry.addExtension(DC_IDENTIFIER);
        doi.setAttributeValue(XSI_TYPE, "dcterms:DOI");
        doi.setText("");

        Element url = entry.addExtension(DC_IDENTIFIER);
        url.setAttributeValue(XSI_TYPE, "dcterms:URL");
        url.setText(handle);

        entry.setTitle(name);
        entry.setUpdated(new Date());
        return entry;
    }

    /**
     * <p>Return a new {@link Collection} created from the contents of the specified {@link Entry}.</p>
     *
     * @param entry Atom {@link Entry} containing the details
     */
    protected Collection collectionFromEntry(Entry entry) throws SQLException, AuthorizeException, IOException {

        String parentId = entry.getExtension(DCTERMS_ISPARTOF) != null?entry.getExtension(DCTERMS_ISPARTOF).getText(): null;
        String collectionId = entry.getExtension(DC_IDENTIFIER) != null?entry.getExtension(DC_IDENTIFIER).getText(): null;
        
        Community parentCommunity = null;
        Collection collection = null;
        
        if (StringUtils.isNotBlank(parentId)) {            
				parentCommunity = Community.find(context, Integer.parseInt(parentId));			
        }

        if (parentCommunity != null)
        	collection = parentCommunity.createCollection();
        else
        	collection = Collection.find(context, Integer.parseInt(collectionId));

        String name = entry.getExtension(DC_TITLE).getText();
        String copyrightText = entry.getExtension(DC_RIGHTS).getText();
        String shortDescription = entry.getExtension(DCTERMS_ALTERNATIVE).getText();
        String introductoryText = entry.getExtension(DCTERMS_ABSTRACT).getText();
        String licenseText = entry.getExtension(DCTERMS_CONFORMSTO).getText();
        String provenanceText = entry.getExtension(DCTERMS_PROVENANCE).getText();

        // If they don't have a name then make it untitled.
        if (StringUtils.isBlank(name)) name = "Untitled";

        // If empty, make it null.
        // @see org.dspace.app.xmlui.aspect.administrative.FlowContainerUtils#processCreateCommunity
        if (StringUtils.isBlank(shortDescription)) shortDescription = null;
        if (StringUtils.isBlank(introductoryText)) introductoryText = null;
        if (StringUtils.isBlank(copyrightText)) copyrightText = null;

        collection.setMetadata("name", name);
        collection.setMetadata("copyright_text", copyrightText);
        collection.setMetadata("short_description", shortDescription);
        collection.setMetadata("introductory_text", introductoryText);
        collection.setMetadata("license", licenseText);
        collection.setMetadata("provenance_description", provenanceText);

        collection.update();
        context.commit();
        
            return collection;
    }

    /**
     * <p>Return this {@link Collection} as an Atom {@link Entry} suitable for
     * inclusion in a web service request or response.  Note that no Atom
     * links will have been defined, because they depend upon the server
     * environment in which these instances are embedded.</p>
     */
    protected Entry collectionAsEntry(Collection collection) {
        Entry entry = abdera.newEntry();

        int id = collection.getID();
        int parentId = -1;
        try {
            parentId = collection.getParentObject().getID();
        } catch (SQLException e) {
            log.error(e);
        }
        String name = collection.getName();
        String handle = collection.getHandle();
        String shortDescription = collection.getMetadata("short_description");
        String introductoryText = collection.getMetadata("introductory_text");
        String copyrightText = collection.getMetadata("copyright_text");
        String licenseText = collection.getMetadata("license");
        String provenanceText = collection.getMetadata("provenance_description");


        entry.addExtension(DC_TYPE).setText("Community");
        entry.addExtension(DC_TITLE).setText(name);
        entry.addExtension(DC_RIGHTS).setText(copyrightText);
        entry.addExtension(DCTERMS_ALTERNATIVE).setText(shortDescription);
        entry.addExtension(DCTERMS_ABSTRACT).setText(introductoryText);
        entry.addExtension(DCTERMS_CONFORMSTO).setText(licenseText);
        entry.addExtension(DCTERMS_PROVENANCE).setText(provenanceText);
        entry.addExtension(DC_IDENTIFIER).setText(String.valueOf(id));
        entry.addExtension(DCTERMS_ISPARTOF).setText(String.valueOf(parentId));

        Element doi = entry.addExtension(DC_IDENTIFIER);
        doi.setAttributeValue(XSI_TYPE, "dcterms:DOI");
        doi.setText("");

        Element url = entry.addExtension(DC_IDENTIFIER);
        url.setAttributeValue(XSI_TYPE, "dcterms:URL");
        url.setText(handle);

        entry.setTitle(name);
        entry.setUpdated(new Date());
        return entry;
    }
}
