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
package org.smartdeveloperhub.vocabulary.util;

import java.net.URI;
import java.net.URL;
import java.util.Set;

import javax.xml.namespace.QName;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Sets;

public final class Namespace implements Comparable<Namespace> {

	public enum Type {
		HASH() {
			@Override
			protected String canonical(final String uri) {
				return
					uri.endsWith("#")?
						uri.substring(0,uri.length()-1):
						uri;
			}
		},
		SLASH() {
			@Override
			protected String canonical(final String uri) {
				return uri;
			}
		},
		;
		private static Namespace.Type fromURI(final String uri) {
			if(uri.endsWith("/")) {
				return Type.SLASH;
			}
			return Type.HASH;
		}

		protected abstract String canonical(final String uri);
	}

	private final String uri;
	private final Type type;
	private final String canonicalForm;

	private Namespace(final String uri) {
		this.uri = uri;
		this.type=Type.fromURI(uri);
		this.canonicalForm=this.type.canonical(uri);
	}

	public String canonicalForm() {
		return this.canonicalForm;
	}

	public String uri() {
		return this.uri;
	}

	public Type type() {
		return this.type;
	}

	public Set<String> variants() {
		final Set<String> result=Sets.newHashSet(this.uri);
		if(Type.HASH.equals(this.type)) {
			if(this.uri.endsWith("#")) {
				result.add(this.uri.substring(0,this.uri.length()-1));
			} else {
				result.add(this.uri+"#");
			}
		}
		return result;
	}

	@Override
	public int hashCode() {
		return this.canonicalForm.hashCode();
	}
	@Override
	public boolean equals(final Object obj) {
		boolean result=false;
		if(obj instanceof Namespace) {
			final Namespace that = (Namespace)obj;
			result=this.canonicalForm.equals(that.canonicalForm);
		}
		return result;
	}

	@Override
	public String toString() {
		return this.uri;
	}

	@Override
	public int compareTo(final Namespace that) {
		return
			ComparisonChain.
				start().
					compare(this.canonicalForm,that.canonicalForm).
					compare(this.uri,that.uri).
					result();
	}

	public static Namespace create(final String uri) {
		return new Namespace(uri);
	}

	public static Namespace create(final URI uri) {
		return create(uri.toString());
	}

	public static Namespace create(final URL uri) {
		return create(uri.toString());
	}

	public static Namespace create(final QName uri) {
		return create(uri.getNamespaceURI()+uri.getLocalPart());
	}

}