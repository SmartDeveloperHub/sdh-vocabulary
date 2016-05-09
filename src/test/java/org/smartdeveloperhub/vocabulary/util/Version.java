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
package org.smartdeveloperhub.vocabulary.util;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

class Version {

	private final Module module;
	private final Namespace vocabulary;
	private final Namespace namespace;
	private final ImmutableSet<Namespace> priorVersions;
	private final ImmutableSet<Namespace> imports;

	private Version(
			final Module module,
			final Namespace vocabulary,
			final Namespace namespace,
			final ImmutableSet<Namespace> priorVersions,
			final ImmutableSet<Namespace> imports) {
		this.module = module;
		this.vocabulary=vocabulary;
		this.namespace=namespace;
		this.priorVersions=priorVersions;
		this.imports = imports;
	}

	Module module() {
		return this.module;
	}

	Namespace vocabulary() {
		return this.vocabulary;
	}

	Namespace namespace() {
		return this.namespace;
	}

	Set<Namespace> priorVersions() {
		return this.priorVersions;
	}

	Set<Namespace> imports() {
		return this.imports;
	}

	boolean isCanonical() {
		return this.vocabulary.equals(this.namespace);
	}

	@Override
	public String toString() {
		final StringBuilder builder=new StringBuilder();
		builder.append("Version(").append(this.module.location()).append(") {").append(System.lineSeparator());
		builder.append(" - Ontology IRI: ").append(this.vocabulary).append(System.lineSeparator());
		builder.append(" - Version IRI.: ").append(this.namespace).append(System.lineSeparator());
		builder.append(" - Imports {").append(System.lineSeparator());
		for(final Namespace ns:this.imports) {
		builder.append("     + ").append(ns).append(System.lineSeparator());
		}
		builder.append("   }").append(System.lineSeparator());
		builder.append(" - Prior versions {").append(System.lineSeparator());
		for(final Namespace ns:this.priorVersions) {
		builder.append("     + ").append(ns).append(System.lineSeparator());
		}
		builder.append("   }").append(System.lineSeparator());
		builder.append("}");
		return builder.toString();
	}

	static Version create(final Module module) {
		return
			new Version(
				module,
				Namespace.create(module.ontology()),
				Namespace.create(implementationIRI(module)),
				priorVersions(module),
				imports(module));
	}

	private static ImmutableSet<Namespace> imports(final Module module) {
		final ImmutableSet.Builder<Namespace> builder=ImmutableSet.builder();
		for(final String priorVersion:module.imports()) {
			builder.add(Namespace.create(priorVersion));
		}
		return builder.build();
	}

	private static ImmutableSet<Namespace> priorVersions(final Module module) {
		final ImmutableSet.Builder<Namespace> builder=ImmutableSet.builder();
		for(final String priorVersion:module.priorVersions()) {
			builder.add(Namespace.create(priorVersion));
		}
		return builder.build();
	}

	private static String implementationIRI(final Module module) {
		if(module.isLocal()) {
			return module.implementationIRI();
		} else {
			return
				module.locationNamespace().toString()+
				MorePaths.getFileExtension(module.location());
		}
	}

}