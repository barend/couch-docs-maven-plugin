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

import java.util.Arrays;
import java.util.Locale;

import com.google.common.base.Preconditions;

/**
 * Encapsulates the plugin configuration.
 *
 * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
 */
class Config {

    public final ExistingDocs existingDocs;
    public final UnknownDatabases unknownDatabases;

    public Config(String existingDocs, String unknownDatabases) {
        this(ExistingDocs.parse(existingDocs), UnknownDatabases.parse(unknownDatabases));
    }

    public Config(ExistingDocs existingDocs, UnknownDatabases unknownDatabases) {
        this.existingDocs = existingDocs;
        this.unknownDatabases = unknownDatabases;
    }

    /**
     * Indicates how to handle an existing document.
     */
    static enum ExistingDocs {

        /**
         * Keep the original, ignore the local document.
         */
        KEEP,

        /**
         * Delete the original, then upload the local document as new.
         */
        REPLACE,

        /**
         * Copy the existing document's {@code _rev} into the local document, then
         * upload the local document as a new version.
         */
        UPDATE,

        /**
         * Fail the build.
         */
        FAIL;

        /**
         * Like {@link #valueOf(String)}, but with case insensitivity and a friendlier error message.
         */
        static ExistingDocs parse(String value) {
            try {
                Preconditions.checkNotNull(value);
                return ExistingDocs.valueOf(value.toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                throw new IllegalArgumentException("The value \"" + value + "\" is not valid; it must be one of "
                        + Arrays.toString(Config.ExistingDocs.values()) + ".");
            }
        }
    }

    /**
     * Indicates how to handle a database that's found in the documents dir but not on the server.
     */
    static enum UnknownDatabases {
        /**
         * Create any missing databases on the server.
         */
        CREATE,

        /**
         * Skip the database.
         */
        SKIP,

        /**
         * Fail the build.
         */
        FAIL;

        /**
         * Like {@link #valueOf(String)}, but with case insensitivity and a friendlier error message.
         */
        static UnknownDatabases parse(String value) {
            try {
                Preconditions.checkNotNull(value);
                return UnknownDatabases.valueOf(value.toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                throw new IllegalArgumentException("The value \"" + value + "\" is not valid; it must be one of "
                        + Arrays.toString(Config.UnknownDatabases.values()) + ".");
            }
        }
    }
}
