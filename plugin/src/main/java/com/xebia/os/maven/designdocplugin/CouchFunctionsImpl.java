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

import com.google.common.base.Optional;

/**
 * Implements {@code CouchFunctions} using {@code HttpUrlConnection}. No fancy stuff like connection pooling or
 * keepalives here, move along.
 *
 * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
 */
class CouchFunctionsImpl implements CouchFunctions {

    @Override
    public boolean isExistentDatabase(String databaseName) {
        return true;
    }

    @Override
    public void createDatabase(String databaseName) {
        // No-op
    }

    @Override
    public Optional<RemoteDesignDocument> download(String databaseName, String id) {
        return Optional.absent();
    }

    @Override
    public void upload(String databaseName, LocalDesignDocument localDocument) {
        // No-op
    }

    @Override
    public void delete(String databaseName, RemoteDesignDocument remoteDocument) {
    }
}
