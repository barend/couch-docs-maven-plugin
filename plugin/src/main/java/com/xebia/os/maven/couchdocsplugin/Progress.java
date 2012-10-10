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

import org.apache.maven.plugin.logging.Log;

/**
 * Handles reporting build progress and the {@code failOnError} behaviour. Isolates the non-Maven code from using the
 * Maven logger.
 *
 * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
 */
class Progress {

    private final boolean failOnError;
    private final Log log;

    public Progress(boolean failOnError, Log log) {
        super();
        this.failOnError = failOnError;
        this.log = log;
    }

    public void debug(String content) {
        log.debug(content);
    }

    public void info(String content) {
        log.info(content);
    }

    public void warn(String content) {
        log.warn(content);
    }

    public void error(String content) {
        error(content, null);
    }

    public void error(String content, Throwable error) {
        if (null != error) {
            log.error(content, error);
        } else {
            log.error(content);
        }
        if (failOnError) {
            throw new RuntimeException(content, error);
        }
    }
}
