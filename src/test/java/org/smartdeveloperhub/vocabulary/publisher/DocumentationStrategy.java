/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015-2016 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.smartdeveloperhub.vocabulary:sdh-vocabulary:0.3.0-SNAPSHOT
 *   Bundle      : sdh-vocabulary-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.vocabulary.publisher;

import java.net.URI;
import java.nio.file.Path;

final class DocumentationStrategy {

	private final Path root;
	private final String namespace;

	DocumentationStrategy(final Path root, final String namespace) {
		this.root = root;
		this.namespace = namespace;
	}

	String rootPath(final URI base, final String iri) {
		final String normalizedIRI = normalize(iri);
		final URI relativePath = base.relativize(URI.create(normalizedIRI));
		String result = toDir(this.namespace)+relativePath;
		if(!result.endsWith("/")) {
			result+="/";
		}
		return result;
	}

	Path assetsPath(final URI base, final String iri) {
		final String normalizedIRI = normalize(iri);
		final URI relativePath = base.relativize(URI.create(normalizedIRI));
		String result = relativePath.toString();
		if(!result.endsWith("/")) {
			result+="/";
		}
		return this.root.resolve(result);
	}

	static String normalize(final String uri) {
		String normalizedIRI=uri;
		if(normalizedIRI.endsWith("#")) {
			normalizedIRI=normalizedIRI.substring(0,normalizedIRI.length()-1);
		}
		return normalizedIRI;
	}

	static String toDir(final String uri) {
		String normalizedIRI=uri;
		if(normalizedIRI.endsWith("#")) {
			normalizedIRI=normalizedIRI.substring(0,normalizedIRI.length()-1);
		}
		if(!normalizedIRI.endsWith("/")) {
			normalizedIRI+="/";
		}
		return normalizedIRI;
	}

	static URI normalize(final URI uri) {
		String normalizedIRI=uri.toString();
		if(normalizedIRI.endsWith("#")) {
			normalizedIRI=normalizedIRI.substring(0,normalizedIRI.length()-1);
		}
		return URI.create(normalizedIRI);
	}

}
