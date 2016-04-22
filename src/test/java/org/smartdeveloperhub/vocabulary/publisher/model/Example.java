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
package org.smartdeveloperhub.vocabulary.publisher.model;

import java.util.Arrays;

import com.google.common.collect.Lists;

public class Example {

	private Example() {
	}

	public static Site site() {
		final Owner owner = owner();
		final Metadata meta = metadata();
		final Ontology ontology = ontology();
		final Site site=new Site();
		site.setTitle("www.smartdeveloperhub.org");
		site.setCopyright("Center for Open Middleware");
		site.setMetadata(meta);
		site.setOwner(owner);
		site.getOntologies().add(ontology);
		return site;
	}

	public static Ontology ontology() {
		final License license = license();

		final Language language = language();

		final Ontology ontology = new Ontology();
		ontology.setId("www.smartdeveloperhub.org.sdh");
		ontology.setUri("http://www.smartdeveloperhub.org/sdh");
		ontology.setTitle("Smart Developer Hub Ontology");
		ontology.getLicenses().add(license);
		ontology.getLanguages().add(language);
		ontology.getDomains().addAll(Lists.newArrayList("ALM","Application Lifecycle Management","Software Engineering","Linked Data"));
		ontology.setSummary("Abbreviated description of the 'Smart Developer Hub vocabulary'");
		ontology.setDescription("Quite a long description of the 'Smart Developer Hub vocabulary'");
		return ontology;
	}

	public static Language language() {
		final Language language=new Language();
		language.setUri("http://lexvo.org/id/iso639-3/eng");
		language.setLabel("en");
		return language;
	}

	public static License license() {
		final License license = new License();
		license.setLabel("CC-BY-NC-SA");
		license.setUri("http://purl.org/NET/rdflicense/cc-by-nc-sa2.0");
		return license;
	}

	public static Metadata metadata() {
		final Metadata meta=new Metadata();
		meta.setApplicationName("Smart Developer Hub Vocabulary Catalog");
		meta.setLanguage("en");
		meta.setDescription("Vocabularies of the Smart Developer Hub project");
		meta.getAuthors().add("Miguel Esteban Gutiérrez");
		meta.getKeywords().addAll(Arrays.asList("Smart Developer Hub","SDH","ALM","Linked Data"));
		return meta;
	}

	public static Owner owner() {
		final Owner owner = new Owner();
		owner.setName("Smart Developer Hub");
		owner.setUri("http://www.smartdeveloperhub.org");
		owner.setLogo("logos/com.symbol.png");
		return owner;
	}


}
