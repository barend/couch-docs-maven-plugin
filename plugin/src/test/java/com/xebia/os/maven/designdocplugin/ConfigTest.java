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

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class ConfigTest {

    @Test(expected = IllegalArgumentException.class)
    public void existingDocsParameterIsRequired() {
        String existingDocs = null;
        new Config(existingDocs, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void existingDocsParameterCannotBeBogus() {
        String existingDocs = "UPSERT"; // Whomever invented this term should be flogged.
        new Config(existingDocs, false);
    }

    @Test
    public void existingDocsParameterMatchesCaseInsensitively() {
        String existingDocs = "RePlAcE";
        final Config config = new Config(existingDocs, false);
        assertEquals(Config.ExistingDocs.REPLACE, config.existingDocs);
    }
}
