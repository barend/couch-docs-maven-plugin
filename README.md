# couch-docs-maven-plugin

## Introduction

This is a Maven plugin to update a [CouchDB][couch] instance with design
documents defined in the project resources. This scratches an itch I has just
prior to a nine hour flight, which is probably how a lot of open source
projects are conceived :-).

## Configuration

For an example of how to configure this project in your pom, consult the
example project.

## How is this different from couchdb-maven-plugin?

At first glance, this plugin appears to be a duplicate of D.T. Hume's
[couchdb-maven-plugin][cmpl]. The difference is that the couchdb-maven-plugin
is designed to assemble [couch apps][couchapp], which come with their own tool
chain and specific filesystem structure. This plugin merely pushes documents
from a local filesystem into a CouchDB instance. This is a subset of the stuff
couchapp gives you. Use whichever fits your needs best.

## License

This software can be used under the terms of the Apache 2.0 License. You can
obtain a copy of this license from the following url:

[http://www.apache.org/licenses/LICENSE-2.0][apache2]

## Issues and TODO's

Issues and TODO's are kept in the [issue tracker][issues] on github.

[apache2]: http://www.apache.org/licenses/LICENSE-2.0
[couch]: http://wiki.apache.org/couchdb/
[issues]: https://github.com/xebia/couch-docs-maven-plugin/issues
[cmpl]: https://github.com/dthume/couchdb-maven-plugin
[couchapp]: http://couchapp.org/
