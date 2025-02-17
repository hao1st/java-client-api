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
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.example.cookbook.Util.ExampleProperties;
import com.marklogic.client.io.InputStreamHandle;

/**
 * DocumentDelete illustrates how to delete a database document.
 */
public class DocumentDelete {
  public static void main(String[] args) throws IOException {
    run(Util.loadProperties());
  }

  public static void run(ExampleProperties props) throws IOException {
    System.out.println("example: "+DocumentDelete.class.getName());

    String filename = "flipper.xml";

    // create the client
    DatabaseClient client = DatabaseClientFactory.newClient(
      props.host, props.port, props.writerUser, props.writerPassword,
      props.authType);

    // create a manager for XML documents
    XMLDocumentManager docMgr = client.newXMLDocumentManager();

    // create an identifier for the document
    String docId = "/example/"+filename;

    setUpExample(docMgr, docId, filename);

    // delete the document
    docMgr.delete(docId);

    System.out.println("Deleted the /example/"+filename+" document");

    // release the client
    client.release();
  }

  // set up by writing document content for the example to delete
  public static void setUpExample(XMLDocumentManager docMgr, String docId, String filename) throws IOException {
    InputStream docStream = Util.openStream("data"+File.separator+filename);
    if (docStream == null)
      throw new IOException("Could not read document example");

    InputStreamHandle handle = new InputStreamHandle();
    handle.set(docStream);

    docMgr.write(docId, handle);
  }
}
