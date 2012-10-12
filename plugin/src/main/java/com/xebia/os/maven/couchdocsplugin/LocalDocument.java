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
import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

/**
 * Implements handling of the JSON files in the local file system.
 *
 * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
 */
class LocalDocument extends Document {
    private final File file;

    public LocalDocument(File file) {
        this.file = Preconditions.checkNotNull(file);
    }

    @VisibleForTesting
    File getFile() {
        return file;
    }

    public void load() throws IOException, DocumentValidationException {
        final JsonParser parser = getJsonFactory().createJsonParser(file);
        initRootNode(parser);
    }

    public void setRev(String rev) {
        Preconditions.checkState(isLoaded(), "Document was not loaded, or loading failed.");
        getRootNode().put("_rev", rev);
    }

    public String getJson() {
        return getRootNode().toString();
    }

    @Override
    public String toString() {
        String result = "LocalDocument[ " + file;
        if (isLoaded()) {
            result += " , " + getId();
        }
        result += " ]";
        return result;
    }
}
