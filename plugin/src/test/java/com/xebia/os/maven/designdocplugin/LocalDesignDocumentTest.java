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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.IOUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class LocalDesignDocumentTest {
    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test(expected = DocumentValidationException.class)
    public void loadShouldEnsureDocIsADesignDoc() throws IOException {
        final File input = newTempFile("/not_a_design_doc.js");
        final LocalDesignDocument ldd = new LocalDesignDocument(input);
        ldd.load();
    }

    @Test
    public void loadShouldAcceptValidDesignDocs() throws IOException {
        final File input = newTempFile("/design_doc.js");
        final LocalDesignDocument ldd = new LocalDesignDocument(input);
        assertFalse(ldd.isLoaded());
        ldd.load();
        assertTrue(ldd.isLoaded());
        assertThat(ldd.getId(), is("_design/Demo"));
        assertFalse(ldd.getRev().isPresent());
    }

    private File newTempFile(final String source) throws IOException, FileNotFoundException {
        File result = temporaryFolder.newFile();
        final InputStream dummyData = UpdateDesignDocsTest.class.getResourceAsStream(source);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(result);
            IOUtil.copy(dummyData, fos);
        } finally {
            IOUtil.close(dummyData);
            IOUtil.close(fos);
        }
        return result;
    }
}
