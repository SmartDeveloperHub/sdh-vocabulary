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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.smartdeveloperhub.vocabulary.util.Module.Format;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public final class Catalog {

	private final class UriFilter implements
			Function<Statement, Optional<String>> {
		private final Result<Module> report;

		private UriFilter(final Result<Module> report) {
			this.report = report;
		}

		@Override
		public Optional<String> apply(final Statement input) {
			final RDFNode object=input.getObject();
			if(object.isURIResource()) {
				return Optional.of(object.asResource().getURI());
			}
			this.report.warn("Discarding invalid value for property %s: not a URI (%s)",input.getPredicate().getURI(),object);
			return Optional.absent();
		}
	}

	private final URI base;
	private final Path root;

	private final List<Module> modules;
	private final Map<String,Integer> moduleIndex;
	private final Map<String,Integer> canonicalModuleIndex;
	private final Map<String,Integer> externalModuleIndex;
	private final Map<String,Integer> ontologyModuleIndex;

	Catalog(final URI base, final Path root) {
		this.base = base;
		this.root = root;
		this.modules=Lists.newArrayList();
		this.moduleIndex=Maps.newLinkedHashMap();
		this.canonicalModuleIndex=Maps.newLinkedHashMap();
		this.externalModuleIndex=Maps.newLinkedHashMap();
		this.ontologyModuleIndex=Maps.newLinkedHashMap();
	}

	private String getRelativePath(final Path file) {
		final Path absoluteBasePath = file.getParent().resolve(getFileName(file));
		final Path relativeBasePath = this.root.relativize(absoluteBasePath);
		return relativeBasePath.toString().replace(File.separatorChar, '/');
	}

	private String getFileName(final Path file) {
		return com.google.common.io.Files.getNameWithoutExtension(file.getFileName().toString());
	}

	private String getFileExtension(final Path file) {
		return com.google.common.io.Files.getFileExtension(file.getFileName().toString());
	}

	private URI getCanonicalNamespace(final Path file) {
		return this.base.resolve(getRelativePath(file));
	}

	private void addModule(final Module module) {
		final int size = this.modules.size();
		this.modules.add(module);
		this.moduleIndex.put(module.relativePath(), size);
		if(module.isExternal()) {
			this.externalModuleIndex.put(module.relativePath(), size);
		}
		if(module.isOntology()) {
			this.ontologyModuleIndex.put(module.relativePath(), size);
			if(module.isCanonical(this.base)) {
				this.canonicalModuleIndex.put(this.base.relativize(URI.create(module.ontology())).toString(),size);
			}
		}
	}

	private void populateOntologyDetails(final ModuleHelper helper, final Result<Module> report, final Module module) {
		final List<Resource> ontologies = helper.getResourcesOfType(owl("Ontology"));
		if(ontologies.isEmpty()) {
			report.warn("No ontology defined");
		} else if(ontologies.size()>1) {
			final List<String> modules=Lists.newArrayList();
			for(final Resource resource:ontologies) {
				modules.add(resource.toString());
			}
			report.warn("More than one ontology defined (%s)",Joiner.on(", ").join(modules));
		} else {
			final Resource resource = ontologies.get(0);
			if(resource.isURIResource()) {
				module.withOntology(resource.asResource().getURI());
			}
			final Function<Statement, Optional<String>> uriFilter=new UriFilter(report);
			final List<String> uris = helper.getPropertyValues(resource, owl("versionIRI"),uriFilter);
			if(uris.isEmpty()) {
				report.warn("No version IRI defined");
			} else if(uris.size()>1) {
				report.warn("Multiple version IRIs defined (%s)",Joiner.on(", ").join(uris));
			} else {
				module.withVersionIRI(uris.get(0));
			}
			module.withImports(helper.getPropertyValues(resource,owl("imports"),uriFilter));
		}
	}

	private void processFile(final Path file, final Format format, final Result<Module> report) {
		final Module module=
			new Module().
				withLocation(file).
				withFormat(format).
				withRelativePath(getRelativePath(file));
		try {
			final ModuleHelper helper=new ModuleHelper(file);
			if(helper.load(getCanonicalNamespace(file),format).isAvailable()) {
				populateOntologyDetails(helper, report, module);
				if(report.errors().isEmpty()) {
					addModule(module);
					report.value(module);
				}
			}
		} catch (final IOException e) {
			report.error("Cannot parse file as %s (%s)",format.getName(),e.getMessage());
		}
	}

	Result<Module> loadModule(final Path file) {
		final Result<Module> report=Result.newInstance();
		final Format format=Format.fromExtension(getFileExtension(file));
		if(format!=null) {
			processFile(file, format, report);
		} else {
			report.error("Cannot process '%s' files",getFileExtension(file));
		}
		return report;
	}

	public Module resolve(final URI ontology) {
		Module result=null;
		final URI relativeURI = this.base.relativize(ontology);
		if(!relativeURI.isAbsolute()) {
			final String relativePath = relativeURI.toString();
			if(this.moduleIndex.containsKey(relativePath)) {
				result=this.modules.get(this.moduleIndex.get(relativePath));
			} else if(this.canonicalModuleIndex.containsKey(relativePath)) {
				result=this.modules.get(this.canonicalModuleIndex.get(relativePath));
			}
		}
		return result;
	}

	public boolean isEmpty() {
		return this.modules.isEmpty();
	}

	public int size() {
		return this.modules.size();
	}

	public List<String> modules() {
		return ImmutableList.copyOf(this.moduleIndex.keySet());
	}

	public List<String> externalModules() {
		return ImmutableList.copyOf(this.externalModuleIndex.keySet());
	}

	public List<String> canonicalModules() {
		return ImmutableList.copyOf(this.canonicalModuleIndex.keySet());
	}

	public List<String> versionModules() {
		final List<String> result = Lists.newArrayList(this.moduleIndex.keySet());
		result.removeAll(this.canonicalModuleIndex.keySet());
		result.removeAll(this.externalModuleIndex.keySet());
		return ImmutableList.copyOf(result);
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("base",this.base).
					add("modules",this.modules).
					add("moduleIndex",this.moduleIndex).
					add("canonicalModuleIndex",this.canonicalModuleIndex).
					add("externalModuleIndex",this.externalModuleIndex).
					add("ontologyModuleIndex",this.ontologyModuleIndex).
					toString();
	}

	private static String owl(final String string) {
		return "http://www.w3.org/2002/07/owl#"+string;
	}

	public URI getBase() {
		return this.base;
	}

}
