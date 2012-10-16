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

import org.junit.Test;

/**
 * Test suite for stuff in {@link Document] that's not covered by its subclasses' tests.
 *
 * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
 */
public class DocumentTest {

    @Test
    public void testIsValidDatabaseName() {
        assertTrue("Database names can be lowercase with hyphens.", Document.isValidDabaseName("database-name"));
        assertFalse("Database names must start with a letter.", Document.isValidDabaseName("1database"));
        assertFalse("Database names cannot contain capitals.", Document.isValidDabaseName("dAtabase1"));
        assertFalse("Database names cannot contain random Unicode.", Document.isValidDabaseName("db-강남스타일")); // Okay, not-so random.
        assertTrue("Database names can contain the $ character.", Document.isValidDabaseName("d$"));
        assertTrue("Database names can contain the ( character.", Document.isValidDabaseName("d("));
        assertTrue("Database names can contain the ) character.", Document.isValidDabaseName("d)"));
        assertTrue("Database names can contain the + character.", Document.isValidDabaseName("d+"));
        assertTrue("Database names can contain the _ character.", Document.isValidDabaseName("d_"));
        assertTrue("Database names can contain the / character.", Document.isValidDabaseName("d/d"));
    }
}
