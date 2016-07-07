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
 *   Artifact    : org.smartdeveloperhub.vocabulary:sdh-vocabulary:0.3.0
 *   Bundle      : sdh-vocabulary-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.vocabulary.util;

import java.net.URI;
import java.nio.file.Path;

import com.google.common.base.Preconditions;

final class Modules {

	private Modules() {
	}

	static boolean isLocatedProperly(final Module module) {
		if(module.isExternal()) {
			return true;
		}
		final URI expectedCanonicalLocation = getExpectedCanonicalLocation(module);
		final URI moduleCanonicalLocation = getPhysicalCanonicalLocation(module);
		return expectedCanonicalLocation.equals(moduleCanonicalLocation);
	}

	static Namespace getOntologyNamespace(final Module module) {
		Preconditions.checkNotNull(module,"Module cannot be null");
		Preconditions.checkArgument(module.isLocal(),"Module should be local");
		return Namespace.create(module.ontology());
	}

	static Namespace getVersionNamespace(final Module module) {
		Preconditions.checkNotNull(module,"Module cannot be null");
		Preconditions.checkArgument(module.isCanonicalVersion(),"Module should be a local version");
		return Namespace.create(module.versionIRI());
	}

	private static URI getExpectedCanonicalLocation(final Module module) {
		String namespace=getModuleNamespace(module);
		final Context context=module.context();
		if(namespace.endsWith("/")) {
			namespace+="index";
		} else if(namespace.endsWith("#")){
			namespace=namespace.substring(0,namespace.length()-1);
		}
		final URI relativeDeclaredURI=context.base().relativize(URI.create(namespace));
		return context.root().toUri().resolve(relativeDeclaredURI);
	}

	private static String getModuleNamespace(final Module module) {
		final String namespace;
		if(module.isVersion()) {
			namespace=module.versionIRI();
		} else {
			namespace=module.ontology();
		}
		return namespace;
	}

	private static URI getPhysicalCanonicalLocation(final Module module) {
		final Path originalLocation = module.location();
		final String fileName = MorePaths.getFileName(originalLocation);
		final Path canonicalLocation = originalLocation.getParent().resolve(fileName);
		return canonicalLocation.toUri();
	}

}
