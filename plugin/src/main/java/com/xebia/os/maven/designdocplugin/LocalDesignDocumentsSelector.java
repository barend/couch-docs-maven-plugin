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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.DirectoryScanner;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

/**
 *
 *
 * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
 */
class LocalDesignDocumentsSelector {
    private static final Pattern COUCH_DATABASENAME_WHITELIST = Pattern.compile("[a-z0-9_$()+-/]+");
    private final File baseDir;
    private final Log log;
    private final String[] includes;
    private final String[] excludes;

    public LocalDesignDocumentsSelector(File baseDir, Log log) {
        this(baseDir, log, new String[] { "**/*.json", "**/*.js" }, null);
    }

    public LocalDesignDocumentsSelector(File baseDir, Log log, String[] includes, String[] excludes) {
        super();
        if (baseDir == null) {
            throw new IllegalArgumentException("The baseDir parameter cannot be null.");
        }
        this.baseDir = baseDir;
        this.log = log;
        this.includes = includes;
        this.excludes = excludes;
    }

    public Multimap<String, File> select() throws IOException {
        if (!baseDir.isDirectory() || !baseDir.canRead()) {
            throw new FileNotFoundException("The path " + baseDir.getPath() + " doesn't exist, is not a directory, or is not readable.");
        }
        Multimap<String, File> result = LinkedListMultimap.create();
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.addDefaultExcludes();
        scanner.setBasedir(baseDir);
        scanner.setIncludes(includes);
        scanner.setExcludes(excludes);
        scanner.scan();
        for (String file : scanner.getIncludedFiles()) {
            String databaseName = new File(file).getParent();
            if (databaseName != null) {
                databaseName = sanifyDatabaseName(databaseName);
                if (databaseName != null) {
                    log.debug("Found document " + file + " in database " + databaseName);
                    result.put(databaseName, new File(baseDir, file));
                } else {
                    log.warn("Ignoring document " + file + " because its directory path contains characters that aren't valid in a Couch database name.");
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
        return (COUCH_DATABASENAME_WHITELIST.matcher(name).matches() ? name : null);
    }
}
