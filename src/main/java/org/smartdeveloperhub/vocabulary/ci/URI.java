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
package org.smartdeveloperhub.vocabulary.ci;

import org.smartdeveloperhub.vocabulary.ci.spi.RDFUtil;

final class URI {

	private final String uriString;

	private final String namespace;

	private final String localName;

	/**
	 * Creates a new URI from the supplied string.
	 *
	 * @param uriString
	 *        A String representing a valid, absolute URI.
	 * @throws IllegalArgumentException
	 *         If the supplied URI is not a valid (absolute) URI.
	 */
	public URI(final String uriString) {
		if(uriString.indexOf(':') < 0) {
			throw new IllegalArgumentException("Not a valid (absolute) URI: " + uriString);
		}
		final int localNameIdx = RDFUtil.getLocalNameIndex(uriString);
		this.uriString = uriString;
		this.localName=uriString.substring(localNameIdx);
		this.namespace=uriString.substring(0, localNameIdx);
	}

	public String stringValue() {
		return this.uriString;
	}

	public String getNamespace() {
		return this.namespace;
	}

	public String getLocalName() {
		return this.localName;
	}

	@Override
	public int hashCode() {
		return this.uriString.hashCode();
	}

	@Override
	public boolean equals(final Object o) {
		if(this==o) {
			return true;
		}

		if(o instanceof URI) {
			return toString().equals(o.toString());
		}

		return false;
	}

	@Override
	public String toString() {
		return this.uriString;
	}
}
