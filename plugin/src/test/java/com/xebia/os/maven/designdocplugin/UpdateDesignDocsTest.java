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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;


@RunWith(MockitoJUnitRunner.class)
public class UpdateDesignDocsTest {

    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Mock private CouchFunctions couchFunctions;
    @Mock private Log log;

    private UpdateDesignDocs createInstance(boolean failOnError, Multimap<String, LocalDesignDocument> localDocs, boolean createDbs) {
        final Config config = new Config("UPDATE", createDbs);
        final Progress progress = new Progress(failOnError, log);
        return new UpdateDesignDocs(config, progress, couchFunctions, localDocs);
    }

    private UpdateDesignDocs createInstance(boolean failOnError, boolean createDbs) {
        Multimap<String, LocalDesignDocument> empty = ImmutableMultimap.of();
        return createInstance(failOnError, empty, createDbs);
    }

    @Test
    public void shouldNotAccessCouchDbForEmptyInput() {
        createInstance(true, false).execute();
        verifyZeroInteractions(couchFunctions);
    }

    @Test
    public void shouldNotAttemptToCreateExistingDatabase() throws IOException {
        Multimap<String, LocalDesignDocument> one = ImmutableMultimap.of("sample", new LocalDesignDocument(temporaryFolder.newFile()));
        when(couchFunctions.isExistentDatabase("sample")).thenReturn(true);
        createInstance(true, one, false).ensureDatabaseExists("sample");
        verify(couchFunctions).isExistentDatabase("sample");
        verifyNoMoreInteractions(couchFunctions);
    }

    @Test
    public void shouldFailBuildIfNotConfiguredToCreateNonExistentDatabase() throws IOException {
        Multimap<String, LocalDesignDocument> one = ImmutableMultimap.of("sample", new LocalDesignDocument(temporaryFolder.newFile()));
        when(couchFunctions.isExistentDatabase("sample")).thenReturn(false);
        try {
            createInstance(true, one, false).ensureDatabaseExists("sample");
            fail("should have thrown an exception");
        } catch(Exception e) {
            verify(couchFunctions).isExistentDatabase("sample");
            verifyNoMoreInteractions(couchFunctions);
        }
    }

    @Test
    public void shouldCreateNonExistentDatabaseIfConfiguredToDoSo() throws IOException {
        Multimap<String, LocalDesignDocument> one = ImmutableMultimap.of("sample", new LocalDesignDocument(temporaryFolder.newFile()));
        when(couchFunctions.isExistentDatabase("sample")).thenReturn(false);
        createInstance(true, one, true).ensureDatabaseExists("sample");
        verify(couchFunctions).isExistentDatabase("sample");
        verify(couchFunctions).createDatabase("sample");
        verifyNoMoreInteractions(couchFunctions);
    }
}
