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
 *   Artifact    : org.smartdeveloperhub.vocabulary:sdh-vocabulary:0.3.0-SNAPSHOT
 *   Bundle      : sdh-vocabulary-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.vocabulary.ci;

import java.util.List;
import java.util.Map;

final class ValueFactory {

	private static final class ValueImpl implements Value {
		private final String lexicalForm;

		private ValueImpl(String lexicalForm) {
			this.lexicalForm = lexicalForm;
		}

		@Override
		public String lexicalForm() {
			return this.lexicalForm;
		}

	}

	private ValueUtils utils;

	private ValueFactory(String base, Map<String,String> namespaceTable) {
		this.utils=new ValueUtils(new URI(base),namespaceTable);
	}

	Value[] qualifiedNames(List<String> types) {
		Value[] v=new Value[types.size()];
		for(int i=0;i<types.size();i++) {
			v[i]=qualifiedName(types.get(i));
		}
		return v;
	}

	Value qualifiedName(String value) {
		return new ValueImpl(value);
	}

	Value uri(String uri) {
		return new ValueImpl(this.utils.writeURI(new URI(uri)));
	}

	Value uriRef(String uri) {
		return new ValueImpl(this.utils.writeURIRef(new URI(uri)));
	}

	Value relativeUri(String relativeUri) {
		return new ValueImpl("<"+relativeUri+">");
	}

	Value blankNode(String id) {
		return new ValueImpl("_:"+id);
	}

	Value literal(String lexicalForm) {
		return new ValueImpl(this.utils.writeLiteral(lexicalForm, null, null));
	}

	Value literal(String lexicalForm, String language) {
		return new ValueImpl(this.utils.writeLiteral(lexicalForm, language, null));
	}

	Value typedLiteral(Object value, String datatype) {
		return new ValueImpl(this.utils.writeLiteral(value.toString(), null, new URI(datatype)));
	}

	static ValueFactory create(String base, Map<String, String> namespaceTable) {
		return new ValueFactory(base,namespaceTable);
	}
}