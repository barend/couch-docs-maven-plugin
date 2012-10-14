# couch-docs-maven-plugin

## Introduction

This is a Maven plugin to update a [CouchDB][couch] instance with design
documents defined in the project resources. This scratches an itch I had just
prior to a nine hour flight, which is probably how a lot of open source
projects are conceived :-).

## Usage

This plugin can be used both as a command and bound to a lifecycle phase. The
design documents are kept as files in your project source directory, with one
file for each design document. The file name is ignored; documents are assumed
to contain an `_id` property. The value of this property is the id that the
plugin will use when uploading the document into CouchDB. All other document
content is used as-is.

### Directory structure

Couch documents get their own sub directory within your `src/` directory.

    .
    ├── pom.xml
    └── src
        └── main
            └── couchdb
                ├── customers
                │   ├── americas
                │   │   └── design_doc.js
                │   ├── apac
                │   │   └── design_doc.js
                │   └── emea
                │       └── design_doc.js
                └── products
                    ├── view_one.js
                    └── view_two.js

The preceding directory structure would upload two documents into the
`products` database and one document each into to the `customers/americas`,
`customers/apac` and `customers/emea` database.

### Pom configuration

The following shows how to configure the plugin for invocation from the command
line:

```xml
    <plugin>
      <groupId>com.xebia.os.couch-docs-maven-plugin</groupId>
      <artifactId>couch-docs-maven-plugin</artifactId>
      <version>0.1-SNAPSHOT</version>
      <configuration>
        <couchUrl>http://localhost:5984</couchUrl>
        <baseDir>src/main/couchdb</baseDir>
        <failOnError>true</failOnError>
        <!--
          unknownDatabases: How to handle databases that exist in your 
          filesystem structure but not in the Couch instance.
          CREATE (default)
              Create the database and upload the design docs.
          SKIP
              Skip over this database, don't upload anything.
          FAIL
              The build fails.
        -->
        <unknownDatabases>CREATE</unknownDatabases>
        <!--
          existingDocs: How to handle documents that already exist in the
          Couch instance.
          KEEP
              The original document is kept in CouchDB unmodified. The local
              document is ignored. A warning is emitted.
          UPDATE (default)
              The _rev of the original document is copied into the local
              document and the local document is then posted to Couch as an
              update. The document history will show a single change.
          REPLACE
              The original document is deleted before the local document is
              uploaded. The document history in CouchDB will show a deletion
              followed by an insertion.
          FAIL
              The build fails.
        -->
        <existingDocs>UPDATE</existingDocs>
      </configuration>
    </plugin>
```

For an example of how to configure the plugin with a lifecyle binding, consult
[the pom of the example project][examplepom]. To get more information about the
plugin parameters, run `mvn help:describe -Dplugin=couch-docs -Ddetail`.

### Invocation

To run as a command, invoke `mvn couch-docs:update` on your command line
after configuring the plugin in your pom. If you created a lifecycle binding,
just run a maven build to the desired lifecycle phase.

## Caveats

 * Each document is parsed using the Jackson parser and the JSON DOM, *not the raw
content from disk*, is serialized to UTF-8 and transferred to CouchDB. This could
cause problems if you use an exotic encoding or very large files.
 * File encoding is detected by the Jackson parser, as per
[JsonFactory.createJsonParser(java.io.file)][jfcjp].
 * The plugin has no notion of CouchDB attachments. Inlining them in your JSON files
will work.

## How is this different from couchdb-maven-plugin?

At first glance, this plugin may appear to be a duplicate of D.T. Hume's
[couchdb-maven-plugin][cmpl]. The difference is that the couchdb-maven-plugin
is designed to assemble [couch apps][couchapp], which come with their own tool
chain and specific filesystem structure. This plugin merely pushes documents
from a local filesystem into a CouchDB instance. This is a subset of the stuff
couchapp gives you. Use whichever fits your needs best.

## Copyright and License

This software is copyright 2012 Xebia Nederland B.V. This software can be used
under the terms of the Apache 2.0 License. You can obtain a copy of this license
from the following url:

[http://www.apache.org/licenses/LICENSE-2.0][apache2]

## Issues and TODO's

Issues and TODO's are kept in the [issue tracker][issues] on github.

[apache2]: http://www.apache.org/licenses/LICENSE-2.0
[couch]: http://wiki.apache.org/couchdb/
[issues]: https://github.com/xebia/couch-docs-maven-plugin/issues
[cmpl]: https://github.com/dthume/couchdb-maven-plugin
[couchapp]: http://couchapp.org/
[jfcjp]: http://jackson.codehaus.org/1.9.9/javadoc/org/codehaus/jackson/JsonFactory.html#createJsonParser(java.io.File)
[examplepom]: https://github.com/xebia/couch-docs-maven-plugin/blob/master/example/pom.xml
