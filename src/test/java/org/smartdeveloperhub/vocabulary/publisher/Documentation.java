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

import org.smartdeveloperhub.vocabulary.util.Module;

class Documentation {

	private final URI base;
	private final String moduleDocumentationRoot;
	private final String landingPage;
	private final String implementationIRI;
	private final String ontologyIRI;
	private final Path path;

	private Documentation(final URI base, final Path path, final String ontologyIRI, final String implementationIRI, final String moduleDocumentation, final String landingPage) {
		this.base = base;
		this.path = path;
		this.ontologyIRI = ontologyIRI;
		this.implementationIRI = implementationIRI;
		this.moduleDocumentationRoot = moduleDocumentation;
		this.landingPage = landingPage;
	}

	String ontologyIRI() {
		return this.ontologyIRI;
	}

	String implementationIRI() {
		return this.implementationIRI;
	}

	boolean needsRedirect() {
		return !this.ontologyIRI.equals(this.implementationIRI);
	}

	String rootPath() {
		return root().getPath();
	}

	String landingPagePath() {
		return landingPage().getPath();
	}

	String redirectionPath() {
		return redirection().getPath();
	}

	URI root() {
		return this.base.resolve(this.moduleDocumentationRoot);
	}

	URI landingPage() {
		return this.base.resolve(this.moduleDocumentationRoot+this.landingPage);
	}

	URI redirection() {
		String str = "html/"+this.base.relativize(URI.create(this.ontologyIRI));
		if(!str.endsWith("/")) {
			str+="/";
		}
		return this.base.resolve(str+this.landingPage);
	}

	private static String implementationIRI(final Module module) {
		String implementationIRI=module.ontology();
		if(module.isVersion()) {
			implementationIRI=module.versionIRI();
		}
		return implementationIRI;
	}

	private static String moduleDocumentationPath(final Module module) {
		String documentationPath="html/"+module.relativePath();
		if(!documentationPath.endsWith("/")) {
			documentationPath+="/";
		}
		return documentationPath;
	}

	static Documentation create(final URI base, final Path path, final Module module) {
		final String implementationIRI=implementationIRI(module);
		final String moduleDocumentation = moduleDocumentationPath(module);
		return new Documentation(base,path,module.ontology(),implementationIRI,moduleDocumentation,"index.html");
	}

	Path assetsPath() {
		return this.path.getParent().resolve(this.moduleDocumentationRoot);
	}

}