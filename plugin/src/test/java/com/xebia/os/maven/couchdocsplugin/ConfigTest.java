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

import org.junit.Test;

import com.xebia.os.maven.couchdocsplugin.Config;


public class ConfigTest {

    @Test(expected = IllegalArgumentException.class)
    public void existingDocsParameterIsRequired() {
        String existingDocs = null;
        String unknownDatabases = "SKIP";
        new Config(existingDocs, unknownDatabases);
    }

    @Test(expected = IllegalArgumentException.class)
    public void existingDocsParameterCannotBeBogus() {
        String existingDocs = "UPSERT"; // Whomever invented this term should be flogged.
        String unknownDatabases = "SKIP";
        new Config(existingDocs, unknownDatabases);
    }

    @Test
    public void existingDocsParameterMatchesCaseInsensitively() {
        String existingDocs = "RePlAcE";
        String unknownDatabases = "SKIP";
        final Config config = new Config(existingDocs, unknownDatabases);
        assertEquals(Config.ExistingDocs.REPLACE, config.existingDocs);
        assertEquals(Config.UnknownDatabases.SKIP, config.unknownDatabases);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownDatabasesParameterIsRequired() {
        String existingDocs = "UPDATE";
        String unknownDatabases = null;
        new Config(existingDocs, unknownDatabases);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownDatabasesParameterCannotBeBogus() {
        String existingDocs = "UPDATE";
        String unknownDatabases = "WRONG";
        new Config(existingDocs, unknownDatabases);
    }

    @Test
    public void unknownDatabasesParameterMatchesCaseInsensitively() {
        String existingDocs = "UPDATE";
        String unknownDatabases = "FaiL";
        final Config config = new Config(existingDocs, unknownDatabases);
        assertEquals(Config.ExistingDocs.UPDATE, config.existingDocs);
        assertEquals(Config.UnknownDatabases.FAIL, config.unknownDatabases);
    }
}
