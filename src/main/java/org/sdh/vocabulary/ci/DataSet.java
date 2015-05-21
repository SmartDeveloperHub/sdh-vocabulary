/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://smartdeveloperhub.github.io/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015 Center for Open Middleware.
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
 *   Artifact    : org.sdh.vocabulary:sdh-vocabulary:1.0.0-SNAPSHOT
 *   Bundle      : sdh-vocabulary-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.sdh.vocabulary.ci;

import java.util.Map;
import java.util.Map.Entry;

import org.sdh.vocabulary.VocabularyUtil;

import com.google.common.collect.Maps;

public final class DataSet {

	private final String base;
	private final Map<String,Build> builds;
	private final Map<String, String> namespaces;
	private String location;

	private DataSet(String base) {
		this.base = base;
		this.builds=Maps.newLinkedHashMap();
		this.namespaces=Maps.newLinkedHashMap();
	}

	public DataSet withNamespace(String prefix, String namespace) {
		this.namespaces.put(prefix, namespace);
		return this;
	}

	public Build withBuild(String id) {
		Build build = this.builds.get(id);
		if(build==null) {
			build=new Build(this,id);
			this.builds.put(build.id(),build);
		}
		return build;
	}

	public String assemble() {
		ValueFactory factory=ValueFactory.create(this.base, this.namespaces);
		StringBuilder builder=new StringBuilder();
		for(Entry<String, String> entry:this.namespaces.entrySet()) {
			builder.
				append("@prefix ").
				append(entry.getKey()).
				append(": ").
				append(factory.uri(entry.getValue()).lexicalForm()).
				append(" .").
				append(System.lineSeparator());
		}
		builder.append(System.lineSeparator());
		builder.
			append("@base ").
			append(factory.uri(this.base).lexicalForm()).
			append(" .").
			append(System.lineSeparator());
		builder.append(System.lineSeparator());
		for(Build build:this.builds.values()) {
			builder.
				append(build.assemble(factory)).
				append(System.lineSeparator());
			for(Execution execution:build.executions()) {
				builder.
					append(execution.assemble(factory)).
					append(System.lineSeparator()).
					append(execution.result().assemble(factory)).
					append(System.lineSeparator());
			}
			builder.append(System.lineSeparator());
		}
		return builder.toString();
	}

	public static DataSet create(String base) {
		return
			new DataSet(base).
				withNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#").
				withNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#").
				withNamespace("owl", "http://www.w3.org/2002/07/owl#").
				withNamespace("xsd", "http://www.w3.org/2001/XMLSchema#").
				withNamespace("sdh", VocabularyUtil.vocabularyNamespace("sdh")).
				withNamespace("platform", VocabularyUtil.vocabularyNamespace("platform")).
				withNamespace("ci", VocabularyUtil.vocabularyNamespace("ci")).
				withNamespace("scm", VocabularyUtil.vocabularyNamespace("scm")).
				withNamespace("org", VocabularyUtil.vocabularyNamespace("organization")).
				withNamespace("metrics", VocabularyUtil.vocabularyNamespace("metrics")).
				withNamespace("oslc", "http://open-services.net/ns/core#").
				withNamespace("oslc_auto", "http://open-services.net/ns/auto#").
				withNamespace("oslc_asset", "http://open-services.net/ns/asset#").
				withNamespace("oslc_config", "http://open-services.net/ns/config#").
				withNamespace("doap", "http://usefulinc.com/ns/doap#").
				withNamespace("foaf", "http://xmlns.com/foaf/0.1/").
				withNamespace("dcterms", "http://purl.org/dc/terms/");

	}

	public DataSet withLocation(String location) {
		this.location=location;
		return this;
	}

	public String location() {
		return this.location;
	}

}