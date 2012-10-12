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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.IOUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.xebia.os.maven.couchdocsplugin.Config;
import com.xebia.os.maven.couchdocsplugin.CouchFunctions;
import com.xebia.os.maven.couchdocsplugin.LocalDocument;
import com.xebia.os.maven.couchdocsplugin.Progress;
import com.xebia.os.maven.couchdocsplugin.RemoteDocument;
import com.xebia.os.maven.couchdocsplugin.UpdateCouchDocs;
import com.xebia.os.maven.couchdocsplugin.Config.ExistingDocs;
import com.xebia.os.maven.couchdocsplugin.Config.UnknownDatabases;


@RunWith(MockitoJUnitRunner.class)
public class UpdateCouchDocsTest {

    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Mock private CouchFunctions couchFunctions;
    @Mock private Log log;

    @Test
    public void shouldUploadNewFileToNewDatabase() throws FileNotFoundException, IOException {
        final Config config = new Config(ExistingDocs.FAIL, UnknownDatabases.CREATE);
        final Progress progress = new Progress(true, log);

        File docFile = newTempFile("/design_doc.js");
        final LocalDocument localDoc = new LocalDocument(docFile);
        Multimap<String, LocalDocument> docs = ImmutableMultimap.of("database", localDoc);

        when(couchFunctions.isExistentDatabase("database")).thenReturn(false);
        when(couchFunctions.download("database", "_design/Demo"))
            .thenReturn(Optional.<RemoteDocument>absent());

        new UpdateCouchDocs(config, progress, couchFunctions, docs).execute();

        verify(couchFunctions).isExistentDatabase("database");
        verify(couchFunctions).createDatabase("database");
        verify(couchFunctions).download("database", "_design/Demo");
        assertFalse(localDoc.getRev().isPresent());
        verify(couchFunctions).upload("database", localDoc);
        verifyNoMoreInteractions(couchFunctions);
    }

    @Test
    public void shouldSkipNonExistingDatabase() throws FileNotFoundException, IOException {
        final Config config = new Config(ExistingDocs.FAIL, UnknownDatabases.SKIP);
        final Progress progress = new Progress(true, log);

        File docFile = newTempFile("/design_doc.js");
        final LocalDocument localDoc = new LocalDocument(docFile);
        ListMultimap<String, LocalDocument> docs = ImmutableListMultimap.of("database2", localDoc, "database", localDoc);

        when(couchFunctions.isExistentDatabase("database2")).thenReturn(false);
        when(couchFunctions.isExistentDatabase("database")).thenReturn(true);
        when(couchFunctions.download("database", "_design/Demo")).thenReturn(Optional.<RemoteDocument>absent());

        new UpdateCouchDocs(config, progress, couchFunctions, docs).execute();

        verify(couchFunctions).isExistentDatabase("database2");
        verify(couchFunctions).isExistentDatabase("database");
        verify(couchFunctions).download("database", "_design/Demo");
        assertFalse(localDoc.getRev().isPresent());
        verify(couchFunctions).upload("database", localDoc);
        verifyNoMoreInteractions(couchFunctions);
    }

    @Test
    public void shouldSkipNonExistingDatabaseIfFailOnErrorIsFalse() throws FileNotFoundException, IOException {
        final Config config = new Config(ExistingDocs.FAIL, UnknownDatabases.FAIL);
        final Progress progress = new Progress(false, log);

        File docFile = newTempFile("/design_doc.js");
        final LocalDocument localDoc = new LocalDocument(docFile);
        ListMultimap<String, LocalDocument> docs = ImmutableListMultimap.of("database2", localDoc, "database", localDoc);

        when(couchFunctions.isExistentDatabase("database2")).thenReturn(false);
        when(couchFunctions.isExistentDatabase("database")).thenReturn(true);
        when(couchFunctions.download("database", "_design/Demo")).thenReturn(Optional.<RemoteDocument>absent());

        new UpdateCouchDocs(config, progress, couchFunctions, docs).execute();

        verify(log).error("Database \"database2\" does not exist.");
        verify(couchFunctions).isExistentDatabase("database2");
        verify(couchFunctions).isExistentDatabase("database");
        verify(couchFunctions).download("database", "_design/Demo");
        assertFalse(localDoc.getRev().isPresent());
        verify(couchFunctions).upload("database", localDoc);
        verifyNoMoreInteractions(couchFunctions);
    }

    @Test
    public void shouldFailOnNonExistingDatabase() throws FileNotFoundException, IOException {
        final Config config = new Config(ExistingDocs.FAIL, UnknownDatabases.FAIL);
        final Progress progress = new Progress(true, log);

        File docFile = newTempFile("/design_doc.js");
        final LocalDocument localDoc = new LocalDocument(docFile);
        Multimap<String, LocalDocument> docs = ImmutableMultimap.of("database", localDoc);

        when(couchFunctions.isExistentDatabase("database")).thenReturn(false);

        try {
            new UpdateCouchDocs(config, progress, couchFunctions, docs).execute();
            fail();
        } catch (RuntimeException e) {
            assertNotNull(e);
        }

        verify(log).error("Database \"database\" does not exist.");
        verify(couchFunctions).isExistentDatabase("database");
        verifyNoMoreInteractions(couchFunctions);
    }

    @Test
    public void shouldUpdateRevIfDocumentExists() throws FileNotFoundException, IOException {
        final Config config = new Config(ExistingDocs.UPDATE, UnknownDatabases.FAIL);
        final Progress progress = new Progress(true, log);

        final RemoteDocument remoteDoc = new RemoteDocument(read("/remote_design_doc.js"));
        final LocalDocument localDoc = new LocalDocument(newTempFile("/design_doc.js"));
        Multimap<String, LocalDocument> docs = ImmutableMultimap.of("database", localDoc);

        when(couchFunctions.isExistentDatabase("database")).thenReturn(true);
        when(couchFunctions.download("database", "_design/Demo")).thenReturn(Optional.of(remoteDoc));

        new UpdateCouchDocs(config, progress, couchFunctions, docs).execute();

        verify(couchFunctions).isExistentDatabase("database");
        verify(couchFunctions).download("database", "_design/Demo");
        assertThat(localDoc.getRev().get(), is(equalTo(remoteDoc.getRev().get())));
        verify(couchFunctions).upload("database", localDoc);
        verifyNoMoreInteractions(couchFunctions);
    }

    @Test
    public void shouldDeleteExistingCopyIfDocumentExists() throws FileNotFoundException, IOException {
        final Config config = new Config(ExistingDocs.REPLACE, UnknownDatabases.FAIL);
        final Progress progress = new Progress(true, log);

        final RemoteDocument remoteDoc = new RemoteDocument(read("/remote_design_doc.js"));
        final LocalDocument localDoc = new LocalDocument(newTempFile("/design_doc.js"));
        Multimap<String, LocalDocument> docs = ImmutableMultimap.of("database", localDoc);

        when(couchFunctions.isExistentDatabase("database")).thenReturn(true);
        when(couchFunctions.download("database", "_design/Demo")).thenReturn(Optional.of(remoteDoc));

        new UpdateCouchDocs(config, progress, couchFunctions, docs).execute();

        verify(couchFunctions).isExistentDatabase("database");
        verify(couchFunctions).download("database", "_design/Demo");
        verify(couchFunctions).delete("database", remoteDoc);
        assertFalse(localDoc.getRev().isPresent());
        verify(couchFunctions).upload("database", localDoc);
        verifyNoMoreInteractions(couchFunctions);
    }

    @Test
    public void shouldSkipUploadIfDocumentExists() throws FileNotFoundException, IOException {
        final Config config = new Config(ExistingDocs.KEEP, UnknownDatabases.FAIL);
        final Progress progress = new Progress(true, log);

        final RemoteDocument remoteDoc = new RemoteDocument(read("/remote_design_doc.js"));
        final LocalDocument localDoc = new LocalDocument(newTempFile("/design_doc.js"));
        Multimap<String, LocalDocument> docs = ImmutableMultimap.of("database", localDoc);

        when(couchFunctions.isExistentDatabase("database")).thenReturn(true);
        when(couchFunctions.download("database", "_design/Demo")).thenReturn(Optional.of(remoteDoc));

        new UpdateCouchDocs(config, progress, couchFunctions, docs).execute();

        verify(couchFunctions).isExistentDatabase("database");
        verify(couchFunctions).download("database", "_design/Demo");
        verifyNoMoreInteractions(couchFunctions);
    }

    @Test
    public void shouldFailBuildIfDocumentExists() throws FileNotFoundException, IOException {
        final Config config = new Config(ExistingDocs.FAIL, UnknownDatabases.FAIL);
        final Progress progress = new Progress(true, log);

        final RemoteDocument remoteDoc = new RemoteDocument(read("/remote_design_doc.js"));
        final LocalDocument localDoc = new LocalDocument(newTempFile("/design_doc.js"));
        Multimap<String, LocalDocument> docs = ImmutableMultimap.of("database", localDoc);

        when(couchFunctions.isExistentDatabase("database")).thenReturn(true);
        when(couchFunctions.download("database", "_design/Demo")).thenReturn(Optional.of(remoteDoc));

        try {
            new UpdateCouchDocs(config, progress, couchFunctions, docs).execute();
            fail();
        } catch (RuntimeException e) {
            assertNotNull(e);
        }

        verify(log).error("Document \"_design/Demo\" already exists in database \"database\"");
        verify(couchFunctions).isExistentDatabase("database");
        verify(couchFunctions).download("database", "_design/Demo");
        verifyNoMoreInteractions(couchFunctions);
    }

    /**
     * The config {@code (ExistingDocs.FAIL & failOnError=false)} should not perform an upload.
     */
    @Test
    public void shouldSkipUploadIfDocumentExistsAndFailOnErrorIsFalse() throws FileNotFoundException, IOException {
        final Config config = new Config(ExistingDocs.FAIL, UnknownDatabases.FAIL);
        final Progress progress = new Progress(false, log);

        final RemoteDocument remoteDoc = new RemoteDocument(read("/remote_design_doc.js"));
        final LocalDocument localDoc = new LocalDocument(newTempFile("/design_doc.js"));
        Multimap<String, LocalDocument> docs = ImmutableMultimap.of("database", localDoc);

        when(couchFunctions.isExistentDatabase("database")).thenReturn(true);
        when(couchFunctions.download("database", "_design/Demo")).thenReturn(Optional.of(remoteDoc));

        new UpdateCouchDocs(config, progress, couchFunctions, docs).execute();

        verify(couchFunctions).isExistentDatabase("database");
        verify(couchFunctions).download("database", "_design/Demo");
        verifyNoMoreInteractions(couchFunctions);
    }

    private File newTempFile(final String source) throws IOException, FileNotFoundException {
        File result = temporaryFolder.newFile();
        final InputStream dummyData = UpdateCouchDocsTest.class.getResourceAsStream(source);
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

    private byte[] read(final String contents) throws IOException, FileNotFoundException {
        final InputStream dummyData = UpdateCouchDocsTest.class.getResourceAsStream(contents);
        if (dummyData == null) {
            throw new FileNotFoundException();
        }
        return IOUtil.toByteArray(dummyData);
    }
}
