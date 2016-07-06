----------------------------------------------------------------------------------------------------
-- Vocabulary Publisher v@{vocabularium.version}-b@{git.commitsCount} (@{git.branch}@@{git.buildnumber})
----------------------------------------------------------------------------------------------------

The purpose of the Vocabulary Publisher is to enable publishing a OWL-based 
vocabularies and their associated documentation.

Given a collection of canonical vocabulary implementations that have a common
base namespace (either hash or slash namespaces) the publisher enables retrieving
alternative RDF serializations of those canonical implementations when derefe-
rencing the vocabularies' canonical IRI and/or version IRI.

For example, for a Turtle serialized vocabulary with canonical IRI 
'http://example.org/vocab/data' and version IRI 'http://example.org/vocab/v1/data', 
the publisher allows retrieving RDF/XML and JSON-LD serializations for each of 
these IRIs, when configured properly, leveraging content negotiation [1].

In addition, the publisher allows retrieving format specific serializations 
for any available vocabularies without taking into account content negotiation.

Thus, in when dereferencing 'http://www.example.org/vocab/v1/data.rdf', the 
publisher would return an RDF/XML serialization of the vocabulary of the 
previous example.

-- Executing the publisher -------------------------------------------------------------------------

The publisher can be executed using the scripts in the 'bin' directory of the 
distribution. The scripts only require one argument, the configuration of the 
publisher:

> publisher.sh <path-to-config-file>

-- Configuration guide -----------------------------------------------------------------------------

The Vocabulary Publisher requires certain information in order to carry out its 
duties. In particular, the base namespace URI, the location of the vocabularies,
network details, and documentation details. All of these 'configuration parameters' 
are defined via a YAML configuration file [2].

The minimal configuration file has the following structure:

	base: <base namespace URI of the vocabularies>
	root: <path to the vocabularies>

The vocabularies must be organized hierarchically within the root 
directory following the same structure as their representation 
IRIs relative to the base namespace.

The representation IRI is the ontology IRI if no version IRI is defined,
or the version IRI in other case. 

Thus for a hash-namespace vocabulary IRI '<base>/vocab/hash', the publisher
expects a canonical file 'vocab/hash.EXT' within the root directory, where
'EXT' is one of rdf, ttl, and jsonld (case insensitive) and determines the
serialization format.

However, for a slash-namespace vocabulary IRI '<base>/vocab/slash/' the
publisher expectes a canonical file 'vocab/slash/index.EXT' within the root
directory.

In addittion, it is also possible to configure the network details, in particular 
the 'host' to which the publisher will be bound and the 'port' used for 
communications. These parameters can be defined including the following 
configuration options: 

	server:
	  host: <ip address or fully qualified domain name>
	  port: <port>

It is worth noting that it is not necessary to configure these network details.
If not specified the publisher uses 'localhost' and 8080 as default values,
respectively.

Finally, if the publisher is to serve any documentation related to the
available vocabularies, the configuration file should include the following
configuration options:

	docs:
	  root: <path to the documentation>
	  relativePath: <path relative to the base namespace URI>

Again, it is worth noting that it is not necessary to configure all these
details. If not specified, the root path will be the 'docs' sibling directory 
of the main root configuration parameter, and 'html' will be the relative path.

The documentation must be structured hierarchically following the namespace
of the associated vocabulary, i.e., for a vocabulary <base>/vocab/name,
the publisher would look for a directory 'vocab/name/' in the documentation
root directory.

-- Limitations -------------------------------------------------------------------------------------

1. The publisher supports negotiating media types. However only Turtle, RDF/XML, 
   and JSON-LD serializations are available. 
2. The publisher supports negotiating character encodings. However only UTF-8, 
   US-ASCII, and ISO-8859-1 encodings are supported.
3. The format of the configured canonical representations is discovered by inspecting 
   the extension of the files, as follows:
   - rdf --> RDF/XML
   - ttl --> Turtle
   - jsonld --> LD-JSON
4. Configured canonical representations must be encoded using UTF-8.
5. Only one canonical representation per namespace is supported.
6. Upon start, the publisher verifies the configured representations and caches
   the alternative serializations. Http requests will be served using these 
   representations.
7. Documentation is only served if configured.
8. If the publisher is configured to serve documentation, but the publisher
   fails to find it, it will return a template HTML stating that the documen-
   tation is not available.

-- References --------------------------------------------------------------------------------------

[1] https://tools.ietf.org/html/rfc7231#section-3.4
[2] http://yaml.org/
----------------------------------------------------------------------------------------------------