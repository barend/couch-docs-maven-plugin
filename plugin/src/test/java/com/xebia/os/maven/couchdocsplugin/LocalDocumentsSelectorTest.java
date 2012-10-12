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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.IOUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonatype.inject.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.xebia.os.maven.couchdocsplugin.LocalDocument;
import com.xebia.os.maven.couchdocsplugin.LocalDocumentsSelector;

@RunWith(MockitoJUnitRunner.class)
public class LocalDocumentsSelectorTest {

    @Rule public TemporaryFolder tempDir = new TemporaryFolder();
    @Mock private Log log;
    private ByteArrayInputStream jsonData;

    @Test
    public void shouldIgnoreJsonFilesInRoot() throws IOException {
        createTempFile("doc3.js");
        createTempFile("doc3.json");

        final LocalDocumentsSelector collector = new LocalDocumentsSelector(tempDir.getRoot(), log);
        final Multimap<String, LocalDocument> selected = collector.select();
        assertEquals("Should find no qualifying files.", 0, selected.size());
    }

    @Test
    public void shouldIgnoreNonJsonFiles() throws IOException {
        final File d2doc1 = createTempFile("database/doc1.json");
        final File d2dud1 = createTempFile("database/readme.txt");

        final LocalDocumentsSelector collector = new LocalDocumentsSelector(tempDir.getRoot(), log);
        final Multimap<String, LocalDocument> selected = collector.select();
        assertEquals("Should find one qualifying file.", 1, selected.size());
        final Collection<LocalDocument> docs = selected.get("database");
        assertFalse("Should ignore non-json files.", Iterables.any(docs, selectedFile(d2dud1)));
        assertTrue("Should find js file in database.", Iterables.any(docs, selectedFile(d2doc1)));
    }

    @Test
    public void shouldFindFilesInMultipleDatabases() throws IOException {
        final File d1doc1 = createTempFile("database1/doc1.js");
        final File d1doc2 = createTempFile("database1/doc2.json");
        final File d2doc1 = createTempFile("database2/doc1.json");

        final LocalDocumentsSelector collector = new LocalDocumentsSelector(tempDir.getRoot(), log);
        final Multimap<String, LocalDocument> selected = collector.select();
        assertEquals("Should find three qualifying files.", 3, selected.size());
        final Collection<LocalDocument> d1docs = selected.get("database1");
        assertTrue("Should find js file in database.", Iterables.any(d1docs, selectedFile(d1doc1)));
        assertTrue("Should find json file in database.", Iterables.any(d1docs, selectedFile(d1doc2)));
        final Collection<LocalDocument> d2docs = selected.get("database2");
        assertTrue("Should find js file in second database.", Iterables.any(d2docs, selectedFile(d2doc1)));
    }

    @Test
    public void shouldSupportSlashesInDbNames() throws IOException {
        final File d2doc1 = createTempFile("customers/japan/doc1.json");
        final LocalDocumentsSelector collector = new LocalDocumentsSelector(tempDir.getRoot(), log);
        final Multimap<String, LocalDocument> selected = collector.select();
        assertEquals("Should find one qualifying file.", 1, selected.size());
        final Collection<LocalDocument> docs = selected.get("customers/japan");
        assertTrue("Should find js file in database.", Iterables.any(docs, selectedFile(d2doc1)));
    }

    @Test
    public void shouldAllowAllSupportedWeirdCharacters() throws IOException {
        final File d2doc1 = createTempFile("ha_$()+-000/doc1.json");
        final LocalDocumentsSelector collector = new LocalDocumentsSelector(tempDir.getRoot(), log);
        final Multimap<String, LocalDocument> selected = collector.select();
        assertEquals("Should find one qualifying file.", 1, selected.size());
        final Collection<LocalDocument> docs = selected.get("ha_$()+-000");
        assertTrue("Should find js file in database.", Iterables.any(docs, selectedFile(d2doc1)));
    }

    @Test
    public void shouldRejectUppercaseInDbNames() throws IOException {
        createTempFile("DATABASE/doc1.json");
        final LocalDocumentsSelector collector = new LocalDocumentsSelector(tempDir.getRoot(), log);
        final Multimap<String, LocalDocument> selected = collector.select();
        assertEquals("Should find no qualifying files.", 0, selected.size());
    }

    @Test
    public void sanifyDatabaseNameConvertsBackslashes() {
        final String name = "some\\path/full\\of\\backslashes";
        assertEquals("some/path/full/of/backslashes", LocalDocumentsSelector.sanifyDatabaseName(name));
    }

    @Before
    public void loadJsonData() throws IOException {
        final InputStream source = LocalDocumentsSelectorTest.class.getResourceAsStream("/design_doc.js");
        try {
            final ByteArrayOutputStream target = new ByteArrayOutputStream();
            IOUtil.copy(source, target);
            jsonData = new ByteArrayInputStream(target.toByteArray());
        } finally {
            IOUtil.close(source);
        }
    }

    private File createTempFile(String pathname) throws IOException {
        FileOutputStream fos = null;
        try {
            File dest = new File(tempDir.getRoot(), pathname);
            dest.getParentFile().mkdirs();
            fos = new FileOutputStream(dest, true);
            IOUtil.copy(jsonData, fos);
            jsonData.reset();
            return dest;
        } finally {
            IOUtil.close(fos);
        }
    }

    private static Predicate<LocalDocument> selectedFile(final File file) {
        return new Predicate<LocalDocument>() {
            public boolean apply(@Nullable LocalDocument doc) {
                return null != doc && file.equals(doc.getFile());
            }
        };
    }
}
