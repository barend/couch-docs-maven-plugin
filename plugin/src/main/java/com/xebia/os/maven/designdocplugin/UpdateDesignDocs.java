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
import org.codehaus.jackson.JsonNode;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;

/**
 * Implements the main workflow.
 *
 * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
 */
class UpdateDesignDocs {

    private final JsonDocumentProcessor documentProcessor;
    private final Progress progress;
    private final CouchFunctions couchFunctions;
    private final Multimap<String, File> localDesignDocuments;
    private final boolean createDbs;

    public UpdateDesignDocs(
            JsonDocumentProcessor documentProcessor,
            Progress progress,
            CouchFunctions couchFunctions,
            Multimap<String, File> localDesignDocuments,
            boolean createDbs) {
        super();
        this.documentProcessor = Preconditions.checkNotNull(documentProcessor);
        this.progress = Preconditions.checkNotNull(progress);
        this.couchFunctions = Preconditions.checkNotNull(couchFunctions);
        this.localDesignDocuments = Preconditions.checkNotNull(localDesignDocuments);
        this.createDbs = createDbs;
    }

    public void execute() {
        for (final String databaseName : localDesignDocuments.keySet()) {
            progress.info("Processing database \"" + databaseName + "\".");
            ensureDatabaseExists(databaseName);
            for (File localDocument : localDesignDocuments.get(databaseName)) {
                processLocalDesignDocument(databaseName, localDocument);
            }
        }
    }

    @VisibleForTesting
    void ensureDatabaseExists(String databaseName) {
        if (!couchFunctions.isExistentDatabase(databaseName)) {
            progress.debug("Database \"" + databaseName + "\" is missing from CouchDB.");
            if (createDbs) {
                progress.info("Creating database \"" + databaseName + "\" in CouchDB.");
                couchFunctions.createDatabase(databaseName);
            } else {
                progress.error("Database " + databaseName + " does not exist.");
            }
        } else {
            progress.debug("Database \"" + databaseName + "\" was found to exist in CouchDB.");
        }
    }

    @VisibleForTesting
    void processLocalDesignDocument(final String databaseName, final File file) {
        progress.debug("Loading file " + file);
        try {
            final JsonNode localDocument = documentProcessor.loadFromDisk(file);
            final String documentId = documentProcessor.assertThatDocumentIsADesignDocAndReturnId(localDocument);
            progress.info("Processing design doucment \"" + documentId + "\".");
        } catch (IOException e) {
            progress.error("Could not load " + file + ": " + e.toString(), e);
        }
    }
}
