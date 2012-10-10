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
package com.xebia.os.maven.couchdocsplugin;

import java.io.IOException;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * Base class for CouchDB design document handling.
 *
 * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
 */
public abstract class DesignDocument {

    private static final JsonFactory JSON_FACTORY = createJsonFactory();

    private ObjectNode jsonRootNode;

    protected void initRootNode(JsonParser parser) throws JsonProcessingException, IOException {
        Preconditions.checkState(jsonRootNode == null, "The rootNode should only be set once.");
        JsonNode parsed = parser.readValueAsTree();

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

    public boolean isLoaded() {
        return null != jsonRootNode;
    }

    public String getId() {
        Preconditions.checkState(isLoaded(), "Document was not loaded, or loading failed.");
        return jsonRootNode.get("_id").asText();
    }

    public Optional<String> getRev() {
        Preconditions.checkState(isLoaded(), "Document was not loaded, or loading failed.");
        JsonNode revNode = jsonRootNode.findPath("_rev");
        if (revNode.isMissingNode()) {
            return Optional.absent();
        }
        return Optional.of(revNode.asText());
    }

    protected ObjectNode getRootNode() {
        return jsonRootNode;
    }

    protected JsonFactory getJsonFactory() {
        return JSON_FACTORY;
    }

    private static JsonFactory createJsonFactory() {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.setCodec(new ObjectMapper());
        return jsonFactory;
    }
}
