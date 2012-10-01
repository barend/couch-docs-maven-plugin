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

class CouchDatabaseException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final int responseCode;

    public CouchDatabaseException(int responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public String toString() {
        return CouchDatabaseException.class.getSimpleName() + ": HTTP " + responseCode + " " + getMessage();
    }
}
