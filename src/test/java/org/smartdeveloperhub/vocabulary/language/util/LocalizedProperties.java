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
package org.smartdeveloperhub.vocabulary.language.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public final class LocalizedProperties {

	private final Map<String,String> properties;

	public LocalizedProperties() {
		this.properties=Maps.newTreeMap();
	}

	public LocalizedProperties(final LocalizedProperties that) {
		this();
		this.properties.putAll(that.properties);
	}

	public void putAll(final Map<String,String> values) {
		this.properties.putAll(values);
	}

	public Set<String> propertyNames() {
		return ImmutableSet.copyOf(this.properties.keySet());
	}

	public String getProperty(final String property) {
		return this.properties.get(property);
	}

	public String getProperty(final String property, final String defaultValue) {
		String result=this.properties.get(property);
		if(result==null) {
			result=defaultValue;
		}
		return result;
	}

	public String setProperty(final String property, final String value) {
		return this.properties.put(property, value);
	}

	public void load(final InputStream stream, final Charset charset) throws IOException {
		final BufferedReader in=new BufferedReader(new InputStreamReader(stream,charset));
		String str;
		int line=0;
		while((str=in.readLine())!=null) {
			line++;
			if(str.startsWith("#")) {
				continue;
			}
			final String[] split=str.split("=");
			if(split.length==1) {
				System.err.println("Ignored line "+line+": '"+str+"'");
				continue;
			}
			this.properties.put(split[0],split[1]);
		}
	}

	public void store(final OutputStream stream, final Charset charset) throws IOException {
		final OutputStreamWriter writer=new OutputStreamWriter(stream, charset);
		for(final Entry<String,String> entry:this.properties.entrySet()) {
			final String line = entry.getKey()+"="+entry.getValue()+"\r";
			writer.write(line);
		}
		writer.flush();
	}

}