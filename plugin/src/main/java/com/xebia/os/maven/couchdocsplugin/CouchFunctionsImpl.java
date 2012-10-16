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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.codehaus.plexus.util.Base64;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.inject.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Implements {@code CouchFunctions} using {@code HttpUrlConnection}. No fancy stuff like connection pooling or
 * keepalives here, move along.
 *
 * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
 */
class CouchFunctionsImpl implements CouchFunctions {

    private final URL baseUrl;
    private final String authorization;
    private static final String HTTP_USER_AGENT = "couch-docs-maven-plugin; Java " + System.getProperty("java.vm.name")
            + " " + System.getProperty("java.vm.version");
    private static final int HTTP_OK = 200;
    private static final int HTTP_CREATED = 201;
    private static final int HTTP_NOTFOUND = 404;

    public CouchFunctionsImpl(String baseUrl) throws MalformedURLException {
        this(new URL(Preconditions.checkNotNull(baseUrl)));
    }

    public CouchFunctionsImpl(URL baseUrl) {
        Preconditions.checkNotNull(baseUrl);
        Preconditions.checkArgument(baseUrl.getProtocol().startsWith("http"), "CouchDB URL must be HTTP or HTTPS");
        this.baseUrl = baseUrl;

        String userInfo = baseUrl.getUserInfo();
        if (!Strings.isNullOrEmpty(userInfo)) {
            this.authorization = "Basic " + new String( Base64.encodeBase64(userInfo.getBytes(Charsets.US_ASCII)), Charsets.US_ASCII);
        } else {
            this.authorization = null;
        }
    }

    @Override
    public boolean isExistentDatabase(String databaseName) throws IOException {
        HttpURLConnection urc = createConnection(databaseName);
        urc.setRequestMethod("HEAD");
        urc.setDoOutput(false);
        if (HTTP_OK == urc.getResponseCode()) {
            return true;
        } else if (HTTP_NOTFOUND == urc.getResponseCode()) {
            return false;
        } else {
            throw databaseException(urc);
        }
    }

    @Override
    public void createDatabase(String databaseName) throws IOException {
        HttpURLConnection urc = createConnection(databaseName);
        urc.setRequestMethod("PUT");
        if (HTTP_CREATED != urc.getResponseCode()) {
            throw databaseException(urc);
        }
    }

    @VisibleForTesting
    void deleteDatabase(String databaseName) throws IOException {
        HttpURLConnection urc = createConnection(databaseName);
        urc.setRequestMethod("DELETE");
        if (HTTP_OK != urc.getResponseCode()) {
            throw databaseException(urc);
        }
    }

    @Override
    public Optional<RemoteDocument> download(String databaseName, String id) throws IOException {
        HttpURLConnection urc = createConnection(databaseName, urlEncode(id));
        urc.setRequestMethod("GET");
        if (HTTP_NOTFOUND == urc.getResponseCode()) {
            return Optional.absent();
        } else if (HTTP_OK == urc.getResponseCode()) {
            return Optional.of(new RemoteDocument(urc.getInputStream()));
        } else {
            throw databaseException(urc);
        }
    }

    @Override
    public void upload(String databaseName, LocalDocument localDocument) throws IOException {
        String payload = localDocument.getJson();
        byte[] body = payload.getBytes(Charsets.UTF_8);

        HttpURLConnection urc = createConnection(databaseName, urlEncode(localDocument.getId()));
        urc.setRequestMethod("PUT");
        urc.setRequestProperty("Content-Type", "application/json;charset=utf8");
        urc.setRequestProperty("Content-Length", Integer.toString(body.length));
        urc.setDoOutput(true);
        final OutputStream os = urc.getOutputStream();
        try {
            IOUtil.copy(body, os);
            os.flush();
        } finally {
            IOUtil.close(os);
        }

        if (HTTP_CREATED != urc.getResponseCode()) {
            throw databaseException(urc);
        }
    }

    @Override
    public void delete(String databaseName, RemoteDocument remoteDocument) throws IOException {
        String documentAndQuery = urlEncode(remoteDocument.getId()) + "?rev=" + remoteDocument.getRev().get();
        HttpURLConnection urc = createConnection(databaseName, documentAndQuery);
        urc.setRequestMethod("DELETE");
        if (HTTP_OK != urc.getResponseCode()) {
            throw databaseException(urc);
        }
    }

    private HttpURLConnection createConnection(String databaseName) throws IOException {
        return createConnection(databaseName, null);
    }

    private HttpURLConnection createConnection(String databaseName, @Nullable String documentAndQuery) throws IOException {
        String suffix = createDatabaseUrlSuffix(databaseName, documentAndQuery);
        HttpURLConnection urc = (HttpURLConnection) new URL(baseUrl, suffix).openConnection();
        urc.setConnectTimeout(30000);
        urc.setReadTimeout(120000);
        urc.setRequestProperty("Accept", "application/json");
        urc.setRequestProperty("User-Agent", HTTP_USER_AGENT);
        if (null != authorization) {
            urc.setRequestProperty("Authorization", authorization);
        }
        return urc;
    }

    private static String createDatabaseUrlSuffix(String databaseName, @Nullable String documentAndQuery) {
        Preconditions.checkArgument(Document.isValidDabaseName(databaseName), "The string \"" + databaseName + "\" is an invalid Couch database name.");

        // URLEncode the '/' and '+' characters in the database name. Skip the trailing slash,
        // because encoding that would make for a very confusing database name.
        final int max = databaseName.endsWith("/") ? databaseName.length() - 1 : databaseName.length();
        final StringBuilder result = new StringBuilder(64);
        for (int i = 0; i < max; i++) {
            final char c = databaseName.charAt(i);
            switch (c) {
            case '+':
                result.append("%2B");
                break;
            case '/':
                result.append("%2F");
                break;
            default:
                result.append(c);
            }
        }

        // The trailing slash is documented in http://wiki.apache.org/couchdb/HTTP_database_API as mandatory.
        result.append('/');

        if (null != documentAndQuery) {
            result.append(documentAndQuery);
        }
        return result.toString();
    }

    private static String urlEncode(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("Every implementation of the Java platform is required to support UTF-8.");
        }
    }

    private static CouchDatabaseException databaseException(HttpURLConnection urc) throws IOException {
        return new CouchDatabaseException(urc.getResponseCode(), urc.getResponseMessage());
    }
}
