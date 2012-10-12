# IDEALS SEAD Deposit Service #

  * [SEAD  Home](http://sead-data.net)

  * [IDEALS  Home](http://ideals.illinois.edu)

### RESTful Web Services Guide ###

The following demonstrates how to use the IDEALS SEAD Web service. The
application is capable of creating, reading, and querying IDEALS DSpace
communities and collections.

### Service API

The API consists of four web resources implemented by the following:

`edu.uiuc.ideals.sead.CommunitiesResource`

The `communities` resource that returns a list of communities.

`edu.uiuc.ideals.sead.CommunityResource`

The `community` resource.

`edu.uiuc.ideals.sead.CollectionsResource`

The `collections` resource that returns a list of collections within a community resource.

`edu.uiuc.ideals.sead.CollectionResource`

The `collection` resource.

The mapping of the URI path space is presented in the following table:

URI path Resource class HTTP methods

`/communities/`

CommunitiesResource

GET, POST

`/communities/{communityId}`

CommunityResource

GET, PUT, DELETE*

`/communities/{communityId}/collections`

CollectionsResource

GET, POST

`/communities/{communityId}/collections/{collectionId}`

CollectionResource

GET, PUT, DELETE*

*Not implemented.

### Example 1: Creating a new community

`POST /communities`

Consumes:

  * application/atom+xml
  * application/atom+xml;type=entry
  * application/xml
  * text/xml
    
    curl -v http://test.ideals.illinois.edu/sead/communities -u email:password -H 'Content-type: application/xml' -X POST -d '@community.xml'

Figure 1a: Client request `community.xml`

    
    <entry xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/">
        <dc:type>Community</dc:type>
        <dc:title>SEAD Test Community</dc:title>
        <dc:rights> ... </dc:rights>
        <dcterms:alternative>Test Community for SEAD Project</dcterms:alternative>
        <dcterms:abstract>This community was created by the IDEALS SEAD Community/Collection REST Service.</dcterms:abstract>
        <dcterms:isPartOf></dcterms:isPartOf>
        <title type="text">SEAD Test Community</title>
        <updated>2012-04-27T20:14:58.373Z</updated>
    </entry>

Figure 1b: Server response

    
    HTTP/1.1 201 Created
    Date: Mon, 30 Apr 2012 22:54:40 GMT
    Server: Apache/2.0.52 (Red Hat) mod_bluestem/0.19 mod_ssl/2.0.52 OpenSSL/0.9.7a mod_jk/1.2.15
    Content-Length: 0
    Content-Type: text/plain

### Example 2: Retrieving a list of communities

`GET /communities`

Produces:

  * application/atom+xml
  * application/atom+xml;type=entry
  * application/xml
  * text/xml
    
    curl -v http://test.ideals.illinois.edu/sead/communities -u email:password -H 'Accept: application/atom+xml;type=feed'

Figure 2: Server response

    
    HTTP/1.1 200 OK
    Date: Mon, 30 Apr 2012 23:54:40 GMT
    Server: Apache/2.0.52 (Red Hat) mod_bluestem/0.19 mod_ssl/2.0.52 OpenSSL/0.9.7a mod_jk/1.2.15
    Transfer-Encoding: chunked
    Content-Type: application/atom+xml
    
    <feed xmlns="http://www.w3.org/2005/Atom">
        <id>collections</id>
        <title type="text">IDEALS DSpace (DEV)</title>
        <updated>2012-04-30T20:56:34.369Z</updated>
        <link href="http://localhost:8080/sead/communities" rel="self" />
        <entry xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/">
            <dc:type>Community</dc:type>
            <dc:title>SEAD</dc:title>
            <dc:rights> ... </dc:rights>
            <dcterms:alternative>SEAD Test Community</dcterms:alternative>
            <dcterms:abstract>This is the introductory text.</dcterms:abstract>
            <dc:identifier>2</dc:identifier>
            <dc:identifier xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="dcterms:URL">123456789/502</dc:identifier>
            <dcterms:isPartOf>-1</dcterms:isPartOf>
            <title type="text">SEAD</title>
            <updated>2012-04-30T20:56:34.372Z</updated>
        </entry>
        <entry xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/">
            <dc:type>Community</dc:type>
            <dc:title>SEAD Test Community</dc:title>
            <dc:rights> ... </dc:rights>
            <dcterms:alternative>Test Community for SEAD Project</dcterms:alternative>
            <dcterms:abstract>This community was created by the IDEALS SEAD Community/Collection REST Service.</dcterms:abstract>
            <dc:identifier>2</dc:identifier>
            <dc:identifier xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="dcterms:URL">123456789/502</dc:identifier>
            <dcterms:isPartOf></dcterms:isPartOf>
            <title type="text">SEAD Test Community</title>
            <updated>2012-04-27T20:14:58.373Z</updated>
        </entry>
    </feed>

### SEAD: Sustainable Environment - Actionable Data

Sustainability science is a new and growing area of research that focuses on
interactions between nature and human activities.

[read on »](http://sead-data.net)

[![](http://sead-data.net/sites/sead-data.net/themes/sead/images/footer-images
/footer-images-02.png)](http://www.nsf.gov)

SEAD is funded by the National Science Foundation under cooperative agreement
**#OCI0940824**

### Partner Institutes

[![](http://sead-data.net/sites/sead-data.net/themes/sead/images/footer-images/footer-images-03.png)](http://illinois.edu/) [![](http://sead-data.net/sites/sead-data.net/themes/sead/images/footer-images/footer-images-04.png)](http://www.indiana.edu/)

[![](http://sead-data.net/sites/sead-data.net/themes/sead/images/footer-images/footer-images-05.png)](http://rpi.edu/)

[![](http://sead-data.net/sites/sead-data.net/themes/sead/images/footer-images/footer-images-06.png)](http://www.umich.edu/)


