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
import java.net.URL;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * Update design documents in CouchDB.
 *
 * This plugin updates design documents in CouchDB.
 *
 * @goal update
 *
 * @author Barend Garvelink <bgarvelink@xebia.com>
 */
public class UpdateDesignDocsMojo extends AbstractMojo {

    /**
     * The URL to the CouchDB instance.
     *
     * Note that this is the server URL, it should not include a database name. If the database
     * requires authentication, the username and password should be provided in the URL.
     *
     * @parameter
     * @required
     */
    private URL couchUrl;

    /**
     * The URL to the CouchDB instance.
     *
     * @parameter default="${basedir}/src/main/couchdb"
     * @required
     */
    private File baseDir;

    /**
     * If true, create any missing databases. If false, break the build if a database is missing.
     *
     * @parameter default=false
     */
    private boolean createDbs;

    /**
     * How to handle existing documents (KEEP, UPDATE, REPLACE, FAIL).
     *
     * Valid options:
     *
     * KEEP    the original document is kept in CouchDB unmodified. The local document
     *         is ignored. A warning is emitted.
     *
     * UPDATE  the _rev of the original document is copied into the local document and
     *         the local document is then posted to Couch as an update.
     *
     * REPLACE the original document is deleted before the local document is uploaded.
     *
     * FAIL    the build fails.
     *
     * @parameter defalt="KEEP"
     */
    private String existingDocs;

    /**
     * @parameter default=true
     */
    private boolean failOnError;

    /**
     * The URL to the CouchDB instance.
     *
     * @parameter expression="${designdocs.skip}"
     */
    private boolean skip;

    public void execute() throws MojoExecutionException
    {
        if (skip) {
            getLog().info("Detected '-Ddesigndocs.skip', skipping execution.");
            return;
        }
        dumpConfig();
        getLog().info( "Hello, world." );
    }

    private void dumpConfig() {
        final Log log = getLog();
        if (log.isDebugEnabled()) {
            log.debug("Using configuration:");
            log.debug("  couchUrl    : " + couchUrl);
            log.debug("  baseDir     : " + baseDir);
            log.debug("  createDbs   : " + createDbs);
            log.debug("  existingDocs: " + existingDocs);
            log.debug("  failOnError : " + failOnError);
        }
    }
}
