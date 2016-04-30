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
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public final class Catalog {

	private final class UriFilter implements Function<Statement, Optional<String>> {

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

	private final class StringFilter implements Function<Statement, Optional<String>> {

		private final Result<Module> report;

		private StringFilter(final Result<Module> report) {
			this.report = report;
		}

		@Override
		public Optional<String> apply(final Statement input) {
			final RDFNode object=input.getObject();
			if(object.isLiteral()) {
				final Literal literal=object.asLiteral();
				if(literal.getDatatypeURI()==null || "http://www.w3.org/2001/XMLSchema#string".equals(literal.getDatatypeURI())) {
					return Optional.of(literal.getLexicalForm());
				}
			}
			this.report.warn("Discarding invalid value for property %s: not a String Literal (%s)",input.getPredicate().getURI(),object);
			return Optional.absent();
		}

	}

	private final Context context;
	private final List<Module> modules;
	private final Map<String,Integer> moduleIndex;
	private final Map<String,Integer> canonicalModuleIndex;
	private final Map<String,Integer> externalModuleNamespaceIndex;
	private final Map<String,Integer> externalModuleImplementationIndex;
	private final Map<String,Integer> ontologyModuleIndex;

	Catalog(final Context context) {
		this.context = context;
		this.modules=Lists.newArrayList();
		this.moduleIndex=Maps.newLinkedHashMap();
		this.canonicalModuleIndex=Maps.newLinkedHashMap();
		this.externalModuleNamespaceIndex=Maps.newLinkedHashMap();
		this.externalModuleImplementationIndex=Maps.newLinkedHashMap();
		this.ontologyModuleIndex=Maps.newLinkedHashMap();
	}

	private void addModule(final Module module) {
		final int size = this.modules.size();
		this.modules.add(module);
		this.moduleIndex.put(module.relativePath(), size);
		if(module.isExternal()) {
			this.externalModuleNamespaceIndex.put(module.relativePath(), size);
			this.externalModuleImplementationIndex.put(this.context.getImplementationPath(module.location()), size);
		} else {
			final boolean canonical = module.isCanonical();
			for(final String covariant:Modules.getOntologyNamespace(module).covariants()) {
				if(canonical) {
					this.canonicalModuleIndex.put(getBase().relativize(URI.create(covariant)).toString(),size);
				} else {
					this.ontologyModuleIndex.put(getBase().relativize(URI.create(covariant)).toString(),size);
				}
			}
			if(module.isCanonicalVersion()) {
				final Namespace namespace=Modules.getVersionNamespace(module);
				for(final String covariant:namespace.covariants()) {
					this.ontologyModuleIndex.put(getBase().relativize(URI.create(covariant)).toString(),size);
				}
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
			} else {
				report.error("Ontology identifier cannot be a blank node (%s)",resource.getId().getLabelString());
			}
			populateVersioningMetadata(helper, report, module, resource);
			populateImports(helper, report, module, resource);
		}
	}

	private void populateImports(final ModuleHelper helper, final Result<Module> report, final Module module, final Resource resource) {
		module.
			withImports(
				helper.getPropertyValues(
					resource,
					owl("imports"),
					new UriFilter(report)));
	}

	// TODO: Enable tweaking what can be validate (report errors) and not (report warning)
	private void populateVersioningMetadata(final ModuleHelper helper, final Result<Module> report, final Module module, final Resource resource) {
		populateVersionIRI(helper, report, module, resource);
		populateVersionInfo(helper,report,module,resource);
		populatePriorVersions(helper, report, module, resource);
		populateBackwardCompatibleVersions(helper, report, module, resource);
		populateIncompatibleVersions(helper, report, module, resource);
	}

	private void populateIncompatibleVersions(final ModuleHelper helper, final Result<Module> report, final Module module, final Resource resource) {
		final List<String> incompatibleWithVersions = helper.getPropertyValues(resource, owl("incompatibleWith"),new UriFilter(report));
		if(!module.isVersion() && !incompatibleWithVersions.isEmpty()) {
			report.warn("Only ontology versions can be incompatible with previous versions (%s)", Joiner.on(", ").join(incompatibleWithVersions));
		} else {
			module.withPriorVersions(incompatibleWithVersions);
		}
	}

	private void populateBackwardCompatibleVersions(final ModuleHelper helper, final Result<Module> report,
			final Module module, final Resource resource) {
		final List<String> backwardCompatibleWithVersions = helper.getPropertyValues(resource, owl("backwardCompatibleWith"),new UriFilter(report));
		if(!module.isVersion() && !backwardCompatibleWithVersions.isEmpty()) {
			report.warn("Only ontology versions can be compatible with previous versions (%s)", Joiner.on(", ").join(backwardCompatibleWithVersions));
		} else {
			module.withPriorVersions(backwardCompatibleWithVersions);
		}
	}

	private void populatePriorVersions(final ModuleHelper helper, final Result<Module> report, final Module module, final Resource resource) {
		final List<String> priorVersions = helper.getPropertyValues(resource, owl("priorVersion"),new UriFilter(report));
		if(!module.isVersion() && !priorVersions.isEmpty()) {
			report.warn("Only ontology versions can have previous versions (%s)", Joiner.on(", ").join(priorVersions));
		} else {
			module.withPriorVersions(priorVersions);
		}
	}

	private void populateVersionIRI(final ModuleHelper helper, final Result<Module> report, final Module module, final Resource resource) {
		final List<String> versionIRIs = helper.getPropertyValues(resource, owl("versionIRI"),new UriFilter(report));
		if(versionIRIs.isEmpty()) {
			report.warn("No version IRI defined");
		} else if(versionIRIs.size()>1) {
			report.warn("Multiple version IRIs defined (%s)",Joiner.on(", ").join(versionIRIs));
		} else {
			module.withVersionIRI(versionIRIs.get(0));
		}
	}

	private void populateVersionInfo(final ModuleHelper helper, final Result<Module> report, final Module module, final Resource resource) {
		final Function<Statement, Optional<String>> stringFilter=new StringFilter(report);
		final List<String> versionInfos = helper.getPropertyValues(resource, owl("versionInfo"),stringFilter);
		if(!module.isVersion()) {
			if(!versionInfos.isEmpty()) {
				report.warn("Only ontology versions can have associated version info (%s)",Joiner.on(", ").join(versionInfos));
			}
		} else {
			if(versionInfos.isEmpty()) {
				report.warn("No version info defined");
			} else if(versionInfos.size()>1) {
				report.warn("Multiple version info defined (%s)",Joiner.on(", ").join(versionInfos));
			} else {
				module.withVersionInfo(versionInfos.get(0));
			}
		}
	}

	private void processFile(final Path file, final Format format, final Result<Module> report) {
		final Module module=
			new Module(this.context).
				withLocation(file).
				withFormat(format);
		try {
			final ModuleHelper helper=new ModuleHelper(file);
			if(helper.load(module.locationNamespace(),module.format()).isAvailable()) {
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
		final Format format=Format.fromExtension(MorePaths.getFileExtension(file));
		if(format!=null) {
			processFile(file, format, report);
		} else {
			report.error("Cannot process '%s' files",MorePaths.getFileExtension(file));
		}
		return report;
	}

	public Path getRoot() {
		return this.context.root();
	}

	public URI getBase() {
		return this.context.base();
	}

	public Module get(final String moduleId) {
		Module result=null;
		final Integer index = this.moduleIndex.get(moduleId);
		if(index!=null) {
			result=this.modules.get(index);
		}
		return result;

	}

	public Module resolve(final URI ontology) {
		Module result=null;
		final URI relativeURI = getBase().relativize(ontology);
		if(!relativeURI.isAbsolute()) {
			final String relativePath = relativeURI.toString();
			if(this.canonicalModuleIndex.containsKey(relativePath)) {
				result=this.modules.get(this.canonicalModuleIndex.get(relativePath));
			} else if(this.ontologyModuleIndex.containsKey(relativePath)) {
				result=this.modules.get(this.ontologyModuleIndex.get(relativePath));
			} else if(this.externalModuleNamespaceIndex.containsKey(relativePath)) {
				result=this.modules.get(this.externalModuleNamespaceIndex.get(relativePath));
			} else if(this.externalModuleImplementationIndex.containsKey(relativePath)) {
				result=this.modules.get(this.externalModuleImplementationIndex.get(relativePath));
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

	public List<String> ontologyModules() {
		return ImmutableList.copyOf(this.ontologyModuleIndex.keySet());
	}

	public List<String> externalModules() {
		return ImmutableList.copyOf(this.externalModuleNamespaceIndex.keySet());
	}

	public List<String> canonicalModules() {
		return ImmutableList.copyOf(this.canonicalModuleIndex.keySet());
	}

	public List<String> versionModules() {
		final List<String> result = Lists.newArrayList(this.moduleIndex.keySet());
		result.removeAll(this.canonicalModuleIndex.keySet());
		result.removeAll(this.externalModuleNamespaceIndex.keySet());
		return ImmutableList.copyOf(result);
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("context",this.context).
					add("modules",this.modules).
					add("moduleIndex",this.moduleIndex).
					add("canonicalModuleIndex",this.canonicalModuleIndex).
					add("externalModuleIndex",this.externalModuleNamespaceIndex).
					add("ontologyModuleIndex",this.ontologyModuleIndex).
					toString();
	}

	private static String owl(final String string) {
		return "http://www.w3.org/2002/07/owl#"+string;
	}

}
