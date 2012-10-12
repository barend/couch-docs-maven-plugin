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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.IOUtil;
import org.junit.Test;

import com.xebia.os.maven.couchdocsplugin.RemoteDocument;


public class RemoteDocumentTest {

    /**
     * Earlier versions of the plugin allowed only document id's starting with
     * {@code _design/}; this restriction has been lifted.
     */
    @Test
    public void loadShouldEnsureDocIsADesignDoc() throws IOException {
        final String fileContents = "/not_a_design_doc.js";
        final byte[] input = read(fileContents);
        final RemoteDocument rd = new RemoteDocument(input);
        assertThat(rd.getId(), is("This id does not start with \"_design/\"."));
    }

    @Test
    public void loadShouldAcceptValidDesignDocs() throws IOException {
        final byte[] input = read("/remote_design_doc.js");
        final RemoteDocument ldd = new RemoteDocument(input);
        assertThat(ldd.getId(), is("_design/Demo"));
        assertThat(ldd.getRev(), is(notNullValue()));
    }

    private byte[] read(final String contents) throws IOException, FileNotFoundException {
        final InputStream dummyData = UpdateCouchDocsTest.class.getResourceAsStream(contents);
        if (dummyData == null) {
            throw new FileNotFoundException();
        }
        return IOUtil.toByteArray(dummyData);
    }
}
