/*
 * Copyright 2012-2019 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.client.example.cookbook;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.Authentication;
import com.marklogic.client.FailedRequestException;
import com.marklogic.client.ForbiddenUserException;
import com.marklogic.client.ResourceNotFoundException;
import com.marklogic.client.ResourceNotResendableException;
import com.marklogic.client.admin.QueryOptionsManager;
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.example.cookbook.Util.ExampleProperties;
import com.marklogic.client.io.InputStreamHandle;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.query.MatchDocumentSummary;
import com.marklogic.client.query.MatchLocation;
import com.marklogic.client.query.MatchSnippet;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;

/**
 * StructuredSearch illustrates searching for documents and iterating over results
 * with structured criteria referencing a constraint defined by options.
 */
public class StructuredSearch {
  static final private String OPTIONS_NAME = "products";

  static final private String[] filenames = {"curbappeal.xml", "flipper.xml", "justintime.xml"};

  public static void main(String[] args)
    throws IOException, ResourceNotFoundException, ForbiddenUserException, FailedRequestException, ResourceNotResendableException
  {
    run(Util.loadProperties());
  }

  public static void run(ExampleProperties props)
    throws IOException, ResourceNotFoundException, ForbiddenUserException, FailedRequestException, ResourceNotResendableException
  {
    System.out.println("example: "+StructuredSearch.class.getName());

    configure(props.host, props.port,
      props.adminUser, props.adminPassword, props.authType);

    search(props.host, props.port,
      props.writerUser, props.writerPassword, props.authType);

    tearDownExample(props.host, props.port,
      props.adminUser, props.adminPassword, props.authType);
  }

  public static void configure(String host, int port, String user, String password, Authentication authType)
    throws FailedRequestException, ForbiddenUserException, ResourceNotFoundException, ResourceNotResendableException
  {
    // connect the client
    DatabaseClient client = DatabaseClientFactory.newClient(host, port, user, password, authType);

    // create a manager for writing query options
    QueryOptionsManager optionsMgr = client.newServerConfigManager().newQueryOptionsManager();

    // construct the query options
    String options =
      "<search:options "+
        "xmlns:search='http://marklogic.com/appservices/search'>"+
        "<search:constraint name='industry'>"+
        "<search:value>"+
        "<search:element name='industry' ns=''/>"+
        "</search:value>"+
        "</search:constraint>"+
        "</search:options>";

    // create a handle to send the query options
    StringHandle writeHandle = new StringHandle(options);

    // write the query options to the database
    optionsMgr.writeOptions(OPTIONS_NAME, writeHandle);

    // release the client
    client.release();
  }

  public static void search(String host, int port, String user, String password, Authentication authType)
    throws IOException, ResourceNotFoundException, ForbiddenUserException, FailedRequestException
  {
    // connect the client
    DatabaseClient client = DatabaseClientFactory.newClient(host, port, user, password, authType);

    setUpExample(client);

    // create a manager for searching
    QueryManager queryMgr = client.newQueryManager();

    // create a query builder for the query options
    StructuredQueryBuilder qb = new StructuredQueryBuilder(OPTIONS_NAME);

    // build a search definition
    StructuredQueryDefinition querydef
      = qb.and(qb.term("neighborhoods"), qb.valueConstraint("industry", "Real Estate"));

    // create a handle for the search results
    SearchHandle resultsHandle = new SearchHandle();

    // run the search
    queryMgr.search(querydef, resultsHandle);

    System.out.println("Matched "+resultsHandle.getTotalResults()+
      " documents with structured query\n");

    // iterate over the result documents
    MatchDocumentSummary[] docSummaries = resultsHandle.getMatchResults();
    System.out.println("Listing "+docSummaries.length+" documents:\n");
    for (MatchDocumentSummary docSummary: docSummaries) {
      String uri = docSummary.getUri();
      int score = docSummary.getScore();

      // iterate over the match locations within a result document
      MatchLocation[] locations = docSummary.getMatchLocations();
      System.out.println("Matched "+locations.length+" locations in "+uri+" with "+score+" score:");
      for (MatchLocation location: locations) {

        // iterate over the snippets at a match location
        for (MatchSnippet snippet : location.getSnippets()) {
          boolean isHighlighted = snippet.isHighlighted();

          if (isHighlighted)
            System.out.print("[");
          System.out.print(snippet.getText());
          if (isHighlighted)
            System.out.print("]");
        }
        System.out.println();
      }
    }

    // release the client
    client.release();
  }

  // set up by writing the document content and options used in the example query
  public static void setUpExample(DatabaseClient client)
    throws IOException, ResourceNotFoundException, ForbiddenUserException, FailedRequestException
  {
    XMLDocumentManager docMgr = client.newXMLDocumentManager();

    try ( InputStreamHandle contentHandle = new InputStreamHandle() ) {

      for (String filename: filenames) {
        InputStream docStream = Util.openStream("data"+File.separator+filename);
        if (docStream == null)
          throw new IOException("Could not read document example");

        contentHandle.set(docStream);

        docMgr.write("/example/"+filename, contentHandle);
      }
    }
  }

  // clean up by deleting the documents and query options used in the example query
  public static void tearDownExample(String host, int port, String user, String password, Authentication authType)
    throws ResourceNotFoundException, ForbiddenUserException, FailedRequestException
  {
    DatabaseClient client = DatabaseClientFactory.newClient(host, port, user, password, authType);

    XMLDocumentManager docMgr = client.newXMLDocumentManager();

    for (String filename: filenames) {
      docMgr.delete("/example/"+filename);
    }

    QueryOptionsManager optionsMgr = client.newServerConfigManager().newQueryOptionsManager();

    optionsMgr.deleteOptions(OPTIONS_NAME);

    client.release();
  }
}
