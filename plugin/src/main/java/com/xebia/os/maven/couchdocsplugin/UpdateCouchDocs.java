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
import java.util.Collection;
import java.util.Map;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;

/**
 * Implements the main workflow.
 *
 * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
 */
class UpdateCouchDocs {

    private final Config config;
    private final Progress progress;
    private final CouchFunctions couchFunctions;
    private final Multimap<String, LocalDocument> localDocuments;

    public UpdateCouchDocs(
            Config config,
            Progress progress,
            CouchFunctions couchFunctions,
            Multimap<String, LocalDocument> localDocuments) {
        super();
        this.config = Preconditions.checkNotNull(config);
        this.progress = Preconditions.checkNotNull(progress);
        this.couchFunctions = Preconditions.checkNotNull(couchFunctions);
        this.localDocuments = Preconditions.checkNotNull(localDocuments);
    }

    public void execute() {
        for (final Map.Entry<String, Collection<LocalDocument>> database : localDocuments.asMap().entrySet()) {
            final String databaseName = database.getKey();
            final Collection<LocalDocument> documents = database.getValue();
            progress.info("Processing directory \"" + databaseName + "\" with " + documents.size() + " document(s).");
            progress.indent();
            try {
                if (ensureDatabaseExists(databaseName)) {
                    for (LocalDocument localDocument : documents) {
                        processLocalDesignDocument(databaseName, localDocument);
                    }
                }
            } catch (IOException e) {
                progress.error("Could not verify whether database " + databaseName + " exists.", e);
            } finally {
                progress.unindent();
            }
        }
    }

    /**
     * @return {@code true} if processing for this database should continue after this method returns.
     */
    private boolean ensureDatabaseExists(String databaseName) throws IOException {
        final boolean exists = couchFunctions.isExistentDatabase(databaseName);
        final boolean result;

        if (exists) {
            result = true;
        } else {
            switch (config.unknownDatabases) {
            case FAIL:
                progress.error("Database \"" + databaseName + "\" does not exist.");
                result = false;
                break;
            case SKIP:
                progress.info("Skipping database \"" + databaseName + "\" because it does not exist on the server.");
                result = false;
                break;
            case CREATE:
                progress.info("Creating database \"" + databaseName + "\" on the server.");
                couchFunctions.createDatabase(databaseName);
                result = true;
                break;
            default:
                throw new AssertionError("Unreachable case clause is reached.");
            }
        }
        return result;
    }

    private void processLocalDesignDocument(final String databaseName, final LocalDocument localDocument) {
        progress.debug("Loading file " + localDocument);
        try {
            localDocument.load();
            final String documentId = localDocument.getId();
            progress.debug("Processing document \"" + documentId + "\" from \"" + databaseName + '/' + localDocument.getFile().getName() + "\".");

        } catch (IOException e) {
            progress.error("Could not load " + localDocument + ": " + e.toString(), e);
        }

        if (localDocument.getRev().isPresent()) {
            progress.warn(localDocument + " contains a _rev field; this will be ignored or overwritten.");
        }

        Optional<RemoteDocument> remoteDocument;
        try {
            remoteDocument = couchFunctions.download(databaseName, localDocument.getId());
        } catch (IOException e) {
            progress.error("Could not load remote document " + localDocument.getId() + " from database " + databaseName, e);

            // If we reach this line then "skip errors" is set. Return from the method to continue processing the next local document.
            return;
        }

        try {
            if (remoteDocument.isPresent()) {
                progress.debug("Document exists on server with revision " + remoteDocument.get().getRev().get() + ".");
                if (resolveConflict(databaseName, localDocument, remoteDocument.get())) {
                    progress.info("Uploading document \"" + localDocument.getId() + "\".");
                    couchFunctions.upload(databaseName, localDocument);
                }
            } else {
                progress.debug("Document does not exist on server.");
                progress.info("Uploading document \"" + localDocument.getId() + "\".");
                couchFunctions.upload(databaseName, localDocument);
            }
        } catch (IOException e) {
            progress.error("Could not upload document " + localDocument + " to CouchDB", e);
        }
    }


    /**
     * @return {@code true} if {@code localDocument} should be uploaded after this method returns.
     */
    private boolean resolveConflict(String databaseName, LocalDocument localDocument, RemoteDocument remoteDocument) throws IOException {
        Preconditions.checkArgument(localDocument.getId().equals(remoteDocument.getId()));
        final boolean result;

        switch (config.existingDocs) {
        case KEEP:
            progress.info("Keeping existing document \"" + localDocument.getId() + "\" in database.");
            result = false;
            break;
        case REPLACE:
            progress.info("Deleting existing document \"" + localDocument.getId() + "\" from database.");
            couchFunctions.delete(databaseName, remoteDocument);
            result = true;
            break;
        case UPDATE:
            progress.info("Merging remote revision into local document \"" + localDocument.getId() + "\".");
            localDocument.setRev(remoteDocument.getRev().get());
            result = true;
            break;
        case FAIL:
            progress.error("Document \"" + localDocument.getId() + "\" already exists in database \"" + databaseName + "\"");
            result = false;
            break;
        default:
            throw new AssertionError("Unreachable case clause is reached.");
        }

        return result;
    }
}
