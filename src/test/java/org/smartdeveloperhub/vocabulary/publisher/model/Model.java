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
package org.smartdeveloperhub.vocabulary.publisher.model;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public final class Model {

	private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

	private Model() {
	}

	public static Site fromYAML(final String value) throws IOException {
		return MAPPER.readValue(value,Site.class);
	}

	public static String toYAML(final Site value) throws IOException {
		return MAPPER.writeValueAsString(value);
	}

	public static Site clone(final Site obj) {
		final Site result=new Site();
		result.setTitle(obj.getTitle());
		result.setCopyright(obj.getCopyright());
		result.setOwner(clone(obj.getOwner()));
		result.setMetadata(clone(obj.getMetadata()));
		for(final Ontology ontology:obj.getOntologies()) {
			result.getOntologies().add(clone(ontology));
		}
		return result;
	}

	static Owner clone(final Owner obj) {
		final Owner result=new Owner();
		result.setUri(obj.getUri());
		result.setName(obj.getName());
		result.setLogo(obj.getLogo());
		return result;
	}

	static Ontology clone(final Ontology obj) {
		final Ontology result=new Ontology();
		result.setId(obj.getId());
		result.setDescription(obj.getDescription());
		result.setSummary(obj.getSummary());
		result.setTitle(obj.getTitle());
		result.setUri(obj.getUri());
		for(final License license:obj.getLicenses()) {
			result.getLicenses().add(clone(license));
		}
		for(final Language license:obj.getLanguages()) {
			result.getLanguages().add(clone(license));
		}
		result.getDomains().addAll(obj.getDomains());
		return result;
	}

	static License clone(final License obj) {
		final License result=new License();
		result.setUri(obj.getUri());
		result.setLabel(obj.getLabel());
		return result;
	}

	static Language clone(final Language obj) {
		final Language result=new Language();
		result.setUri(obj.getUri());
		result.setLabel(obj.getLabel());
		return result;
	}

	static Metadata clone(final Metadata obj) {
		final Metadata result=new Metadata();
		result.setApplicationName(obj.getApplicationName());
		result.setLanguage(obj.getLanguage());
		result.setDescription(obj.getDescription());
		result.getAuthors().addAll(obj.getAuthors());
		result.getKeywords().addAll(obj.getKeywords());
		return result;
	}

}
