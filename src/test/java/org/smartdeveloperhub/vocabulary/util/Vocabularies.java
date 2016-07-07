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

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

final class Vocabularies {


	private final ImmutableMap<Namespace, Vocabulary> vocabularies;
	private final ImmutableMap<Namespace, Namespace> versions;

	public Vocabularies(final ImmutableMap<Namespace, Vocabulary> vocabularies, final ImmutableMap<Namespace, Namespace> versions) {
		this.vocabularies = vocabularies;
		this.versions = versions;
	}

	Vocabulary resolveVocabulary(final Namespace target) {
		Vocabulary result = this.vocabularies.get(target);
		if(result==null) {
			final Namespace vocabNamespace = this.versions.get(target);
			result=this.vocabularies.get(vocabNamespace);
		}
		return result;
	}

	Version resolveVersion(final Namespace target) {
		final Namespace vocabNamespace = this.versions.get(target);
		final Vocabulary vocabulary=this.vocabularies.get(vocabNamespace);
		Version version=null;
		if(vocabulary!=null) {
			version=vocabulary.version(target);
		}
		return version;
	}

	List<String> validate() {
		final List<String> failures=Lists.newArrayList();
		for(final Vocabulary vocabulary:this.vocabularies.values()) {
			failures.addAll(vocabulary.validate(this));
		}
		return failures;
	}

	Vocabulary resolveVocabulary(final String namespace) {
		return resolveVocabulary(Namespace.create(namespace));
	}

	Version resolveVersion(final String namespace) {
		return resolveVersion(Namespace.create(namespace));
	}

	@Override
	public String toString() {
		final StringBuilder builder=new StringBuilder();
		builder.append("Vocabularies {").append(System.lineSeparator());
		for(final Vocabulary version:this.vocabularies.values()) {
			builder.
				append(" - ").
				append(StringUtil.indent(version.toString(),"   ")).
				append(System.lineSeparator());
		}
		builder.append("}");
		return builder.toString();
	}


	static Vocabularies create(final Catalog catalog) {
		final Map<Namespace,Vocabulary.Builder> builders=Maps.newLinkedHashMap();
		for(final String moduleId:catalog.modules()) {
			final Module module = catalog.get(moduleId);
			final Version version=Version.create(module);
			Vocabulary.Builder builder=builders.get(version.vocabulary());
			if(builder==null) {
				builder=Vocabulary.create(version.vocabulary());
				builders.put(version.vocabulary(), builder);
			}
			builder.addVersion(version);
		}
		final ImmutableMap.Builder<Namespace,Vocabulary> vocabularies=ImmutableMap.builder();
		final ImmutableMap.Builder<Namespace,Namespace> versionIndex=ImmutableMap.builder();
		for(final Vocabulary.Builder vBuilder:builders.values()) {
			final Vocabulary vocabulary = vBuilder.build();
			final Namespace namespace = vocabulary.namespace();
			vocabularies.put(namespace,vocabulary);
			for(final Namespace version:vocabulary.versions()) {
				versionIndex.put(version,namespace);
			}
		}
		return new Vocabularies(vocabularies.build(),versionIndex.build());
	}

}