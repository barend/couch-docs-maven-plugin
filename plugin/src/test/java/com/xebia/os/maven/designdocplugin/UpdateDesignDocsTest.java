package com.xebia.os.maven.designdocplugin;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
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

    private UpdateDesignDocs createInstance(boolean failOnError, Multimap<String, File> localDocs, boolean createDbs) {
        return new UpdateDesignDocs(new Progress(failOnError, log), couchFunctions, localDocs, createDbs);
    }

    @Test
    public void shouldNotAccessCouchDbForEmptyInput() {
        Multimap<String, File> empty = ImmutableMultimap.of();
        createInstance(true, empty, false).execute();
        verifyZeroInteractions(couchFunctions);
    }

    @Test
    public void shouldNotAttemptToCreateExistingDatabase() throws IOException {
        Multimap<String, File> one = ImmutableMultimap.of("sample", temporaryFolder.newFile());
        when(couchFunctions.isExistentDatabase("sample")).thenReturn(true);
        createInstance(true, one, false).execute();
        verify(couchFunctions).isExistentDatabase("sample");
        verifyNoMoreInteractions(couchFunctions);
    }

    @Test
    public void shouldFailBuildIfNotConfiguredToCreateNonExistentDatabase() throws IOException {
        Multimap<String, File> one = ImmutableMultimap.of("sample", temporaryFolder.newFile());
        when(couchFunctions.isExistentDatabase("sample")).thenReturn(false);
        try {
            createInstance(true, one, false).execute();
            fail("should have thrown an exception");
        } catch(Exception e) {
            verify(couchFunctions).isExistentDatabase("sample");
            verifyNoMoreInteractions(couchFunctions);
        }
    }

    @Test
    public void shouldCreateNonExistentDatabaseIfConfiguredToDoSo() throws IOException {
        Multimap<String, File> one = ImmutableMultimap.of("sample", temporaryFolder.newFile());
        when(couchFunctions.isExistentDatabase("sample")).thenReturn(false);
        createInstance(true, one, true).execute();
        verify(couchFunctions).isExistentDatabase("sample");
        verify(couchFunctions).createDatabase("sample");
        verifyNoMoreInteractions(couchFunctions);
    }
}
