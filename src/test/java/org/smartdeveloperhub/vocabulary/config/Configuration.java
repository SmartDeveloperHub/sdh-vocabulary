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
package org.smartdeveloperhub.vocabulary.config;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class Configuration {

	@JsonIgnore
	private final Map<String, Object> extensions = Maps.newLinkedHashMap();

	public Configuration() {
		super();
	}

	@JsonAnyGetter
	public Map<String,Object> getExtension() {
		return this.extensions;
	}

	@JsonAnySetter
	public void setExtension(final String name, final Object value) {
		this.extensions.put(name,value);
	}

	public void extend(final Object value) {
		setExtension(getExtensionId(value.getClass()), value);
	}

	public Set<String> extensions() {
		return ImmutableSet.copyOf(this.extensions.keySet());
	}

	public <T> T extension(final String id, final Class<? extends T> clazz) {
		return ConfigurationFactory.convert(this.extensions.get(id),clazz);
	}

	public <T> T extension(final Class<? extends T> clazz) {
		final String id = getExtensionId(clazz);
		final Object source = this.extensions.get(id);
		if(source==null) {
			return null;
		}
		return ConfigurationFactory.convert(source,clazz);
	}

	private <T> String getExtensionId(final Class<? extends T> clazz) {
		final ConfigurationExtension annotation = clazz.getAnnotation(ConfigurationExtension.class);
		String id=null;
		if(annotation!=null) {
			id=annotation.value();
		} else {
			id=clazz.getSimpleName();
		}
		Preconditions.checkArgument(id!=null,"Unknown extension %s",clazz.getCanonicalName());
		return id;
	}

}