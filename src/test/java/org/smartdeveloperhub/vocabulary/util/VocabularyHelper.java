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
 *   Artifact    : org.smartdeveloperhub.vocabulary:sdh-vocabulary:0.4.0-SNAPSHOT
 *   Bundle      : sdh-vocabulary-0.4.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.vocabulary.util;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

final class VocabularyHelper {

	private final OntModel vocabulary;
	private final Module module;

	VocabularyHelper(final Model model, final Module module) {
		this.module=module;
		final OntDocumentManager mgr = new OntDocumentManager();
		mgr.setProcessImports(false);
		final OntModelSpec spec=new OntModelSpec(OntModelSpec.OWL_MEM);
		spec.setDocumentManager(mgr);
		this.vocabulary=ModelFactory.createOntologyModel(spec,model);
	}

	Multimap<String,String> namespacePrefixes() {
		final Multimap<String,String> prefixes=LinkedHashMultimap.create();
		for(final Entry<String,String> entry:this.vocabulary.getNsPrefixMap().entrySet()) {
			prefixes.put(entry.getValue(),entry.getKey());
		}
		return prefixes;
	}

	Set<String> prefixes() {
		final Namespace namespace = Namespace.create(this.module.ontology());
		final Multimap<String, String> prefixes = namespacePrefixes();
		final SortedSet<String> nsPrefixes=Sets.newTreeSet();
		for(final String ns:namespace.variants()) {
			final Collection<String> collection = prefixes.get(ns);
			if(collection!=null) {
				nsPrefixes.addAll(collection);
			}
		}
		return nsPrefixes;
	}

	List<String> classes() {
		return extractOntologicalResourceURIs(this.vocabulary.listClasses());
	}
	List<String> datatypeProperties() {
		return extractOntologicalResourceURIs(this.vocabulary.listDatatypeProperties());
	}

	List<String> objectProperties() {
		return extractOntologicalResourceURIs(this.vocabulary.listObjectProperties());
	}

	List<String> individuals() {
		return extractOntologicalResourceURIs(this.vocabulary.listIndividuals());
	}

	List<String> uriRefs(final String... namespaces) {
		final Set<String> valid=Sets.newHashSet(namespaces);
		if(valid.isEmpty()) {
			valid.addAll(this.vocabulary.getNsPrefixMap().values());
		}
		final SortedSet<String> named=Sets.newTreeSet();
		named.addAll(extractResourceURIs(this.vocabulary.listSubjects(),valid));
		named.addAll(extractResourceURIs(this.vocabulary.listAllOntProperties(),valid));
		named.addAll(extractResourceURIs(this.vocabulary.listObjects(),valid));
		return Lists.newArrayList(named);
	}

	private <T> List<String> extractResourceURIs(final ExtendedIterator<T> iterator, final Set<String> namespaces) {
		try {
			final List<String> uris=Lists.newLinkedList();
			while(iterator.hasNext()) {
				final T item=iterator.next();
				if(item instanceof Resource) {
					final Resource resource = (Resource)item;
					if(!resource.isAnon()) {
						final String uri = resource.getURI();
						if(!isReserved(uri)) {
							if(namespaces.contains(resource.getNameSpace())) {
								uris.add(uri);
							}
						}
					}
				}
			}
			return uris;
		} finally {
			iterator.close();
		}
	}

	private boolean isReserved(final String uri) {
		return
			uri.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#") ||
			uri.startsWith("http://www.w3.org/2000/01/rdf-schema#") ||
			uri.startsWith("http://www.w3.org/2001/XMLSchema#") ||
			uri.startsWith("http://www.w3.org/2002/07/owl#");
	}

	private List<String> extractOntologicalResourceURIs(final ExtendedIterator<? extends OntResource> iterator) {
		try {
			final List<String> uris=Lists.newLinkedList();
			while(iterator.hasNext()) {
				final OntResource resource = iterator.next();
				if(!resource.isAnon()) {
					uris.add(resource.getURI());
				}
			}
			return uris;
		} finally {
			iterator.close();
		}
	}

	static VocabularyHelper create(final Module module) throws IOException {
		Objects.requireNonNull(module, "Module cannot be null");
		Preconditions.checkArgument(module.isOntology(),"Module %s is not an ontology",module.location());
		return
			new VocabularyHelper(
				new ModuleHelper(module.location()).
					load(URI.create(module.ontology()),module.format()).
					export(),
				module);
	}

}
