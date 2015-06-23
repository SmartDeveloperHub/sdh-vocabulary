/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
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
 *   Artifact    : org.smartdeveloperhub.vocabulary:sdh-vocabulary:1.0.0-SNAPSHOT
 *   Bundle      : sdh-vocabulary-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.vocabulary.ci;

import java.util.Map;
import java.util.Map.Entry;

import org.smartdeveloperhub.vocabulary.ci.spi.RDFUtil;

import com.google.common.collect.Maps;

final class ValueUtils {

	static enum Namespace {
		RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#",1),
		RDFS("http://www.w3.org/2000/01/rdf-schema#",2),
		OWL("http://www.w3.org/2002/07/owl#",3),
		XML_SCHEMA("http://www.w3.org/2001/XMLSchema#",4),
		UNKNOWN("UNKNOWN",5),
		;
		private final String id;
		private final int priority;

		Namespace(String id, int priority) {
			this.id = id;
			this.priority = priority;
		}

		static Namespace fromURI(URI uri) {
			for(Namespace namespace:values()) {
				if(namespace.getId().equals(uri.getNamespace())) {
					return namespace;
				}
			}
			return UNKNOWN;
		}

		int compare(Namespace n) {
			return priority-n.priority;
		}

		String getId() {
			return id;
		}

	}

	private static final char TAB = '\t';
	private static final char LINE_FEED = '\r';
	private static final char NEW_LINE = '\n';
	private static final String ESCAPED_MULTI_LINE_QUOTES = "\"\"\"";
	private static final String ESCAPED_DOUBLE_QUOTES = "\"";
	private final Map<String, String> namespaceTable;
	private final URI base;

	ValueUtils(URI base, Map<String,String> namespaceTable) {
		this.base = base;
		this.namespaceTable = Maps.newLinkedHashMap();
		for(Entry<String, String> entry:namespaceTable.entrySet()) {
			this.namespaceTable.put(entry.getValue(), entry.getKey());
		}
	}

	String writeLiteral(String label, String language, URI datatype) {
		StringBuilder builder=new StringBuilder();

		if(isMultiLineString(label)) {
			// Write label as long string
			builder.append(ESCAPED_MULTI_LINE_QUOTES);
			builder.append(RDFUtil.encodeLongString(label));
			builder.append(ESCAPED_MULTI_LINE_QUOTES);
		} else {
			// Write label as normal string
			builder.append(ESCAPED_DOUBLE_QUOTES);
			builder.append(RDFUtil.encodeString(label));
			builder.append(ESCAPED_DOUBLE_QUOTES);
		}

		if(language!=null) {
			// Append the literal's language
			builder.append("@");
			builder.append(language);
		} else if(datatype!=null) {
			// TODO: This should be configurable
			if(!canOmmitDatatype(datatype)) {
				/**
				 * Append the literal's datatype (possibly written as an abbreviated
				 * URI)
				 */
				builder.append("^^");
				builder.append(writeURIRef(datatype));
			}
		}
		return builder.toString();
	}

	String writeURI(URI uri) {
		return String.format("<%s>",RDFUtil.encodeURIString(uri.toString()));
	}

	String writeURIRef(URI uri) {
		String result=null;
		String prefix=this.namespaceTable.get(uri.getNamespace());
		if(prefix!=null) {
			// Namespace is mapped to a prefix; write abbreviated URI
			result=String.format("%s:%s",prefix,uri.getLocalName());
		} else {
			// Namespace is not mapped to a prefix; write the resolved URI
			result=String.format("<%s>",RDFUtil.encodeURIString(resolve(uri).toString()));
		}
		return result;
	}

	private java.net.URI resolve(URI uri) {
		java.net.URI resolved = toURI(uri);
		if(base!=null) {
			resolved = toURI(base).relativize(resolved);
		}
		return resolved;
	}

	private java.net.URI toURI(URI uri) {
		return java.net.URI.create(uri.toString()).normalize();
	}

	private boolean canOmmitDatatype(URI datatype) {
		return
			datatype.getLocalName().equals("string") &&
			datatype.getNamespace().equals("http://www.w3.org/2001/XMLSchema#");
	}

	private boolean isMultiLineString(String label) {
		return
			label.indexOf(NEW_LINE)!=-1 ||
			label.indexOf(LINE_FEED)!= -1 ||
			label.indexOf(TAB) != -1;
	}

}