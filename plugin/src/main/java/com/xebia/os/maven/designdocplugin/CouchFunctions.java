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

import java.io.IOException;

import com.google.common.base.Optional;

/**
 * Abstracts CouchDB access functions to aid testability.
 *
 * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
 */
interface CouchFunctions {
    boolean isExistentDatabase(String databaseName) throws IOException;
    void createDatabase(String databaseName) throws IOException;
    Optional<RemoteDesignDocument> download(String databaseName, String id) throws IOException;
    void upload(String databaseName, LocalDesignDocument localDocument) throws IOException;
    void delete(String databaseName, RemoteDesignDocument remoteDocument) throws IOException;
}
