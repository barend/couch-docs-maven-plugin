# Introduction

This is a Maven plugin to update a CouchDB instance with design documents
defined in the project resources. This scratches an itch I has just prior
to a nine hour flight, which is probably how a lot of open source projects
are conceived :-).

# Configuration

For an example of how to configure this project in your pom, consult the
example project.

# License

This software can be used under the terms of the Apache 2.0 License. You can
obtain a copy of this license from the following url:

[http://www.apache.org/licenses/LICENSE-2.0][apache2]

# TODO's

 * Detect design documents in CouchDB that aren't in the source code
   and offer to keep or delete them, or fail the build.
 * Add includes and excludes glob patterns to the configuration.
 * Take advantage of Maven 3's encrypted credential store.

[apache2]: http://www.apache.org/licenses/LICENSE-2.0