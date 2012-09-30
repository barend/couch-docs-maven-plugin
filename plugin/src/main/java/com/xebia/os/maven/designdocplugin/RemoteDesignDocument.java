/*
   Copyright 2012 Xebia Nederland B.V.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.xebia.os.maven.designdocplugin;

import java.io.IOException;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.google.common.base.Preconditions;

/**
 * Implements handling of JSON for documents obtained from CouchDB.
 *
 * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
 */
class RemoteDesignDocument {

    private ObjectNode jsonRootNode;

    public RemoteDesignDocument(byte[] data) throws IOException, DocumentValidationException {
        Preconditions.checkNotNull(data, "data argument cannot be null");
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.setCodec(new ObjectMapper());

        final JsonParser parser = jsonFactory.createJsonParser(data);
        final JsonNode parsed = parser.readValueAsTree();

        if (!parsed.isObject()) {
            throw new DocumentValidationException("The root of the JSON document must be an object node.");
        }

        ObjectNode rootNode = ((ObjectNode) parsed);

        // All design documents have an _id...
        final JsonNode idNode = rootNode.findPath("_id");
        if (!idNode.isTextual()) {
            throw new DocumentValidationException("The document's _id node is missing or not a string value.");
        }

        // ...that starts with "_design/"
        final String id = idNode.asText();
        if (!id.startsWith("_design/")) {
            throw new DocumentValidationException("The value \"" + id + "\" of the document's _id node does begin with \"_design/\".");
        }
        jsonRootNode = rootNode;
    }

    public String getId() {
        return jsonRootNode.get("_id").asText();
    }

    public String getRev() {
        return jsonRootNode.get("_rev").asText();
    }

    @Override
    public String toString() {
        return "RemoteDesignDocument[ " + getId() + " ]";
    }
}
