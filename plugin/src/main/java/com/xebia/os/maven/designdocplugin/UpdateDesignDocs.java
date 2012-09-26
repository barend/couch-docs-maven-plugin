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
import java.util.Collection;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;

/**
 * Implements the main workflow.
 *
 * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
 */
class UpdateDesignDocs {

    private final Progress progress;
    private final CouchFunctions couchFunctions;
    private final Multimap<String, File> localDesignDocuments;
    private final boolean createDbs;

    public UpdateDesignDocs(Progress progress,
            CouchFunctions couchFunctions,
            Multimap<String, File> localDesignDocuments,
            boolean createDbs) {
        super();
        this.progress = Preconditions.checkNotNull(progress);
        this.couchFunctions = Preconditions.checkNotNull(couchFunctions);
        this.localDesignDocuments = Preconditions.checkNotNull(localDesignDocuments);
        this.createDbs = createDbs;
    }

    public void execute() {
        final Map<String, Collection<File>> docs = localDesignDocuments.asMap();
        for (Map.Entry<String, Collection<File>> entry : docs.entrySet()) {
            final String databaseName = entry.getKey();
            final Collection<File> documents = entry.getValue();
            progress.debug("Processing database " + databaseName + " with " + documents.size() + " local design docs.");
            ensureDatabaseExists(databaseName);
        }
    }

    private void ensureDatabaseExists(String databaseName) {
        if (!couchFunctions.isExistentDatabase(databaseName)) {
            if (createDbs) {
                progress.info("Creating database \"" + databaseName + "\" in Couch.");
                couchFunctions.createDatabase(databaseName);
            } else {
                progress.error("Database " + databaseName + " does not exist.");
            }
        }
    }
}
