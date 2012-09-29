package com.xebia.os.maven.designdocplugin;

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
