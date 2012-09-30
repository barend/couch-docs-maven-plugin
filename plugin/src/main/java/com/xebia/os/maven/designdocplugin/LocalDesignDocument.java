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

import java.io.File;
import java.io.IOException;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * Implements handling of the JSON files in the local file system.
 *
 * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
 */
class LocalDesignDocument {
    private final File file;
    private final JsonFactory jsonFactory;
    private JsonNode jsonRootNode;

    public LocalDesignDocument(File file) {
        this.file = Preconditions.checkNotNull(file);
        jsonFactory = new JsonFactory();
        jsonFactory.setCodec(new ObjectMapper());
    }

    @VisibleForTesting
    File getFile() {
        return file;
    }

    public void load() throws IOException, DocumentValidationException {
        final JsonParser parser = jsonFactory.createJsonParser(file);
        JsonNode parsed = parser.readValueAsTree();

        // All design documents have an _id...
        final JsonNode idNode = parsed.findPath("_id");
        if (!idNode.isTextual()) {
            throw new DocumentValidationException("The document's _id node is missing or not a string value.");
        }

        // ...that starts with "_design/"
        final String id = idNode.asText();
        if (!id.startsWith("_design/")) {
            throw new DocumentValidationException("The value \"" + id + "\" of the document's _id node does begin with \"_design/\".");
        }
        jsonRootNode = parsed;
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

    @Override
    public String toString() {
        return "LocalDesignDocument[ " + file + " ]";
    }
}
