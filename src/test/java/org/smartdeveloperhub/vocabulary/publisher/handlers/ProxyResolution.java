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
package org.smartdeveloperhub.vocabulary.publisher.handlers;

import java.net.URI;

import org.smartdeveloperhub.vocabulary.util.Module;

public final class ProxyResolution {

	public static final class Builder {

		private final URI requestURI;
		private URI resolvedURI;
		private Module module;
		private String fragment;

		private Builder(final URI requestURI) {
			this.requestURI = requestURI;
		}

		public Builder module(final Module module) {
			this.module = module;
			return this;
		}

		public Builder resolved(final URI resolvedURI) {
			this.resolvedURI = resolvedURI;
			return this;
		}

		public Builder fragment(final String fragment) {
			this.fragment = fragment;
			return this;
		}

		public ProxyResolution build() {
			return new ProxyResolution(this.requestURI,this.resolvedURI,this.module,this.fragment);
		}

	}

	private final Module module;
	private final String fragment;
	private final URI requestURI;
	private final URI resolvedURI;

	private ProxyResolution(final URI requestURI, final URI resolvedURI, final Module module, final String fragment) {
		this.requestURI = requestURI;
		this.resolvedURI = resolvedURI;
		this.module = module;
		this.fragment = fragment;
	}

	public Module target() {
		return this.module;
	}

	public URI requestedURI() {
		return this.requestURI;
	}

	public URI resolvedURI() {
		return this.resolvedURI;
	}

	public String fragment() {
		return this.fragment;
	}

	public boolean isFragment() {
		return this.fragment!=null;
	}

	static Builder builder(final URI requestURI) {
		return new Builder(requestURI);
	}


}
