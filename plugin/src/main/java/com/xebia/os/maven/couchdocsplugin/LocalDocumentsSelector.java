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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.DirectoryScanner;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

/**
 * Finds the local files that should be processed as Couch documents. Relies on the directory scanner provided by
 * Maven so include/exclude semantics match other Maven plugins.
 *
 * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
 */
class LocalDocumentsSelector {
    private final File baseDir;
    private final Log log;
    private final String[] includes;
    private final String[] excludes;

    public LocalDocumentsSelector(File baseDir, Log log) {
        this(baseDir, log, new String[] { "**/*.json", "**/*.js" }, null);
    }

    public LocalDocumentsSelector(File baseDir, Log log, String[] includes, String[] excludes) {
        super();
        if (baseDir == null) {
            throw new IllegalArgumentException("The baseDir parameter cannot be null.");
        }
        this.baseDir = baseDir;
        this.log = log;
        this.includes = includes;
        this.excludes = excludes;
    }

    /**
     * Finds the local documents for processing.
     * @return keys: database name, values: unloaded {@code LocalDocument}s.
     */
    public Multimap<String, LocalDocument> select() throws IOException {
        if (!baseDir.isDirectory() || !baseDir.canRead()) {
            throw new FileNotFoundException("The path " + baseDir.getPath() + " doesn't exist, is not a directory, or is not readable.");
        }
        Multimap<String, LocalDocument> result = LinkedListMultimap.create();
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.addDefaultExcludes();
        scanner.setBasedir(baseDir);
        scanner.setIncludes(includes);
        scanner.setExcludes(excludes);
        scanner.scan();
        for (String file : scanner.getIncludedFiles()) {
            String databaseName = new File(file).getParent();
            if (databaseName != null) {
                String cleanDatabaseName = sanifyDatabaseName(databaseName);
                if (cleanDatabaseName != null) {
                    log.debug("Found document " + file + " in database " + cleanDatabaseName);
                    result.put(cleanDatabaseName, new LocalDocument(new File(baseDir, file)));
                } else {
                    log.warn("Ignoring document " + file + " because \"" + databaseName + "\" is an invalid Couch database name.");
                }
            } else {
                log.debug("Ingoring document " + file + " in database null");
            }
        }
        return result;
    }

    /**
     * Checks the database name against the white list of supported characters.
     *
     * CouchDB database names can contain the slash, but not the back slash. This is normalized before checking, to
     * make sure the plugin can work on Windows systems. We don't convert to lowercase, cause we just want people to
     * do that right.
     *
     * @return the database name, with backslashes converted to slashes, if valid. Returns {@code null} if invalid.
     */
    public static String sanifyDatabaseName(String databaseName) {
        String name = databaseName.replace('\\', '/');
        return (Document.isValidDabaseName(name) ? name : null);
    }
}
