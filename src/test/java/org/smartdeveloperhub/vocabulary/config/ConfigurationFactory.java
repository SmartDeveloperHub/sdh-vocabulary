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
package org.smartdeveloperhub.vocabulary.config;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;

import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

public final class ConfigurationFactory {
	private ConfigurationFactory() {
	}

	private static YAMLFactory yamlFactory() {
		return
			new YAMLFactory().
				disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).
				disable(YAMLGenerator.Feature.CANONICAL_OUTPUT).
				enable(YAMLGenerator.Feature.SPLIT_LINES).
				enable(YAMLGenerator.Feature.MINIMIZE_QUOTES).
				enable(YAMLGenerator.Feature.USE_NATIVE_OBJECT_ID).
				enable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID);
	}

	private static ObjectMapper parsingMapper() {
		return new ObjectMapper(yamlFactory());
	}

	private static ObjectMapper writingMapper() {
		final YAMLFactory factory = yamlFactory();
		factory.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
		final ObjectMapper mapper = new ObjectMapper(factory);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		return mapper;
	}


	static <T extends Configuration> T load(final URL url, final Class<? extends T> clazz) throws IOException {
		final ObjectMapper mapper = parsingMapper();
		try(InputStream openStream = url.openStream()) {
			return mapper.readValue(openStream,clazz);
		}
	}

	static <T> T convert(final Object source, final Type type) {
		final ObjectMapper mapper = parsingMapper();
		final JavaType constructType = mapper.getTypeFactory().constructType(type);
		checkArgument(mapper.canDeserialize(constructType),"%s is not a valid configuration class",constructType.toCanonical());
		return mapper.convertValue(source,constructType);
	}

	public static <T extends Configuration> T load(final String value, final Class<? extends T> clazz) throws IOException {
		final Yaml yaml=new Yaml();
		final Object load=yaml.load(value);
		return convert(load,clazz);
	}

	public static Configuration load(final String value) throws IOException {
		return load(value,Configuration.class);
	}

	public static String save(final Configuration config) throws IOException {
		return writingMapper().writeValueAsString(config);
	}

}
