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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;

/**
 * Implements the main workflow.
 *
 * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
 */
class UpdateDesignDocs {

    private final Config config;
    private final Progress progress;
    private final CouchFunctions couchFunctions;
    private final Multimap<String, LocalDesignDocument> localDesignDocuments;

    public UpdateDesignDocs(
            Config config,
            Progress progress,
            CouchFunctions couchFunctions,
            Multimap<String, LocalDesignDocument> localDesignDocuments) {
        super();
        this.config = Preconditions.checkNotNull(config);
        this.progress = Preconditions.checkNotNull(progress);
        this.couchFunctions = Preconditions.checkNotNull(couchFunctions);
        this.localDesignDocuments = Preconditions.checkNotNull(localDesignDocuments);
    }

    public void execute() {
        for (final String databaseName : localDesignDocuments.keySet()) {
            progress.info("Processing database \"" + databaseName + "\".");
            if (ensureDatabaseExists(databaseName)) {
                for (LocalDesignDocument localDocument : localDesignDocuments.get(databaseName)) {
                    processLocalDesignDocument(databaseName, localDocument);
                }
            }
        }
    }

    @VisibleForTesting
    boolean ensureDatabaseExists(String databaseName) {
        final boolean exists = couchFunctions.isExistentDatabase(databaseName);
        boolean result = false;

        if (!exists) {
            progress.debug("Database \"" + databaseName + "\" is missing from CouchDB.");
            switch (config.unknownDatabases) {
            case FAIL:
                progress.error("Database " + databaseName + " does not exist.");
                break;
            case SKIP:
                progress.warn("Database " + databaseName + " does not exist. Skipping.");
                break;
            case CREATE:
                progress.info("Creating database \"" + databaseName + "\" in CouchDB.");
                couchFunctions.createDatabase(databaseName);
                result = true;
                break;
            default:
                throw new AssertionError("Unreachable switch clause.");
            }
        }
        return result;
    }

    @VisibleForTesting
    void processLocalDesignDocument(final String databaseName, final LocalDesignDocument document) {
        progress.debug("Loading file " + document);
        try {
            document.load();
            final String documentId = document.getId();
            progress.info("Processing design doucment \"" + documentId + "\".");

            if (document.getRev().isPresent()) {
                progress.warn(document + " contains a _rev field; this will be ignored or overwritten.");
            }

            // Load remote document

            // Check conflict behaviour, modify local _rev if needed

            // Upload local document

        } catch (IOException e) {
            progress.error("Could not load " + document + ": " + e.toString(), e);
        }
    }
}
