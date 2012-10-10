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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.plexus.util.IOUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import com.google.common.base.Optional;
import com.xebia.os.maven.couchdocsplugin.Config;
import com.xebia.os.maven.couchdocsplugin.CouchFunctionsImpl;
import com.xebia.os.maven.couchdocsplugin.RemoteDesignDocument;
import com.xebia.os.maven.couchdocsplugin.UpdateDesignDocsMojo;
import com.xebia.os.maven.couchdocsplugin.junit.ConditionalTestRunner;
import com.xebia.os.maven.couchdocsplugin.junit.EnvironmentCondition;


@RunWith(ConditionalTestRunner.class)
@EnvironmentCondition(name = "COUCHDB_INTEGRATION_TESTS", kind = EnvironmentCondition.Kind.ENVIRONMENT_VARIABLE)
public class UpdateDesignDocsMojoTest {

    private static final String COUCH_URL = "http://admin:admin@localhost:5984";
    private static final String DATABASE1 = "database1-" + new SecureRandom().nextLong();
    private static final String DATABASE2 = "database2-" + new SecureRandom().nextLong();
    private static final String DATABASE3 = "database3-" + new SecureRandom().nextLong();

    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private Map<Object, Object> pluginContext = new HashMap<Object, Object>();
    private Log log = new SystemStreamLog();

    @Before
    public void copyDesignDocs() throws IOException {
        newTempFile("/design_doc.js", DATABASE1, "design1.js");
        newTempFile("/design_doc.js", DATABASE2, "ignored.js");
        newTempFile("/design_doc.js", DATABASE3, "design3.js");
    }

    @Before
    @After
    public void deleteDatabases() throws IOException {
        CouchFunctionsImpl couch = new CouchFunctionsImpl(new URL(COUCH_URL));
        if (couch.isExistentDatabase(DATABASE1)) couch.deleteDatabase(DATABASE1);
        if (couch.isExistentDatabase(DATABASE2)) couch.deleteDatabase(DATABASE2);
        if (couch.isExistentDatabase(DATABASE3)) couch.deleteDatabase(DATABASE3);
    }

    @Test
    public void runMojo() throws IOException, MojoExecutionException {
        final URL couchUrl = new URL(COUCH_URL);
        final UpdateDesignDocsMojo mojo = new UpdateDesignDocsMojo();
        mojo.setBaseDir(temporaryFolder.getRoot());
        mojo.setCouchUrl(couchUrl);
        mojo.setExcludes(new String[] { "**/ignored.js" });
        mojo.setIncludes(new String[] { "**/*.js" });
        mojo.setExistingDocs(Config.ExistingDocs.UPDATE.name());
        mojo.setFailOnError(true);
        mojo.setLog(log);
        mojo.setSkip(false);
        mojo.setPluginContext(pluginContext);
        mojo.setUnknownDatabases(Config.UnknownDatabases.CREATE.name());

        mojo.execute();

        // CouchFuncitonsImpl has been tested separately, use it here to verify the results.
        CouchFunctionsImpl couch = new CouchFunctionsImpl(couchUrl);
        final Optional<RemoteDesignDocument> database1design = couch.download(DATABASE1, "_design/Demo");
        assertTrue("Database 1 design should have been uploaded.", database1design.isPresent());

        final Optional<RemoteDesignDocument> database2design = couch.download(DATABASE2, "_design/Demo");
        assertFalse("Database 2 design should have been ignored.", database2design.isPresent());

        final Optional<RemoteDesignDocument> database3design = couch.download(DATABASE3, "_design/Demo");
        assertTrue("Database 3 design should have been uploaded.", database3design.isPresent());
    }


    private File newTempFile(final String source, final String database, final String name)
            throws IOException, FileNotFoundException {
        File dir = temporaryFolder.newFolder(database);
        File result = new File(dir, name);
        result.createNewFile();

        final InputStream dummyData = UpdateDesignDocsMojoTest.class.getResourceAsStream(source);
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
