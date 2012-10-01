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
import java.net.URL;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.google.common.collect.Multimap;

/**
 * Update design documents in CouchDB.
 *
 * Use this plugin to ensure the design documents in a CouchDb instance are in sync with those in the
 * project resources. This plugin compares the documents found in CouchDB to those found in the source
 * directory and if any differences exist it can either ignore them, fail the build, or bring CouchDB
 * in sync with the source directory.
 *
 * @goal update
 *
 * @author Barend Garvelink <bgarvelink@xebia.com>
 */
public class UpdateDesignDocsMojo extends AbstractMojo {

    /**
     * The URL to the CouchDB instance.
     *
     * <p>This is the server URL, it should not include a database name. If the database
     * requires authentication, the username and password should be provided in the URL.</p>
     *
     * @parameter expression="${designdocs.couchUrl}" default-value="http://localhost:5984"
     * @required
     */
    private URL couchUrl;

    /**
     * The directory where design documents are kept. This directory should contain a sub directory
     * for every database in your couch instance. This sub directory, in turn, contains the design docs.
     *
     * @parameter expression="${designdocs.baseDir}" default-value="src/main/couchdb"
     * @required
     */
    private File baseDir;

    /**
     * How to handle nonexistent databases (CREATE, SKIP, FAIL).
     *
     * <p>Valid options:</p>
     * <dl>
     * <dt>CREATE</dt>
     * <dd>Create the database and upload the design docs.</dd>
     *
     * <dt>SKIP</dt>
     * <dd>Skip over this database, don't upload anything.</dd>
     *
     * <dt>FAIL</dt>
     * <dd>The build fails.</dd>
     * </dl>
     *
     * @parameter expression="${designdocs.unknownDatabases}" default-value="CREATE"
     */
    private String unknownDatabases;

    /**
     * How to handle existing documents (KEEP, UPDATE, REPLACE, FAIL).
     *
     * <p>Valid options:</p>
     * <dl>
     * <dt>KEEP<dt>
     * <dd>The original document is kept in CouchDB unmodified. The local document
     * is ignored. A warning is emitted.</dd>
     *
     * <dt>UPDATE</dt>
     * <dd>The _rev of the original document is copied into the local document and
     * the local document is then posted to Couch as an update. The document
     * history will show a single change.</dd>
     *
     * <dt>REPLACE</dt>
     * <dd>The original document is deleted before the local document is uploaded.
     * The document history in CouchDB will show a deletion followed by an insertion.</dd>
     *
     * <dt>FAIL</dt>
     * <dd>The build fails.</dd>
     * </dl>
     *
     * @parameter expression="${designdocs.existingDocs}" default-value="UPDATE"
     */
    private String existingDocs;

    /**
     * If set to true, the build breaks when an error is encountered. If set to false, a
     * warning is logged in such case.
     *
     * @parameter expression="${designdocs.failOnError}" default-value=true
     */
    private boolean failOnError;

    /**
     * The URL to the CouchDB instance.
     *
     * @parameter expression="${designdocs.skip}"
     */
    private boolean skip;

    /**
     * The include pattern to match design documents.
     *
     * <p>Default: {@code ** /*.json} and {@code ** /*.js}.</p>
     *
     * @parameter
     */
    private String[] includes = new String[] { "**/*.json", "**/*.js" };

    /**
     * The exclude pattern to match design documents.
     *
     * <p>Default: null</p>
     *
     * @parameter
     */
    private String[] excludes;

    public void execute() throws MojoExecutionException
    {
        if (skip) {
            getLog().info("Detected '-Ddesigndocs.skip', skipping execution.");
            return;
        }
        dumpConfig();
        try {
            final Multimap<String, LocalDesignDocument> localDocuments = findLocalDesignDocuments();
            Config config = new Config(existingDocs, unknownDatabases);
            Progress progress = new Progress(failOnError, getLog());
            CouchFunctions couch = new CouchFunctionsImpl(couchUrl);
            new UpdateDesignDocs(config, progress, couch, localDocuments).execute();
        } catch (RuntimeException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private Multimap<String, LocalDesignDocument> findLocalDesignDocuments() throws MojoExecutionException {
        final LocalDesignDocumentsSelector docsCollector = new LocalDesignDocumentsSelector(baseDir, getLog(), includes, excludes);
        try {
            return docsCollector.select();
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void dumpConfig() {
        final Log log = getLog();
        if (log.isDebugEnabled()) {
            log.debug("Using configuration:");
            log.debug("  couchUrl        : " + couchUrl);
            log.debug("  baseDir         : " + baseDir);
            log.debug("  unknownDatabases: " + unknownDatabases);
            log.debug("  existingDocs    : " + existingDocs);
            log.debug("  failOnError     : " + failOnError);
        }
    }

    public void setCouchUrl(URL couchUrl) {
        this.couchUrl = couchUrl;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setUnknownDatabases(String unknownDatabases) {
        this.unknownDatabases = unknownDatabases;
    }

    public void setExistingDocs(String existingDocs) {
        this.existingDocs = existingDocs;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes == null ? null : excludes.clone();
    }

    public void setIncludes(String[] includes) {
        this.includes = includes == null ? null : includes.clone();
    }
}
