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
package org.smartdeveloperhub.vocabulary.publisher.handlers;

import java.net.URI;

import org.ldp4j.xml.XMLUtils;
import org.smartdeveloperhub.vocabulary.publisher.handlers.ProxyResolution.Builder;
import org.smartdeveloperhub.vocabulary.publisher.util.Location;
import org.smartdeveloperhub.vocabulary.publisher.util.Tracing;
import org.smartdeveloperhub.vocabulary.util.Catalog;
import org.smartdeveloperhub.vocabulary.util.Module;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

final class ModuleReverseProxyHandler implements HttpHandler {

	private final Catalog catalog;
	private final HttpHandler next;

	ModuleReverseProxyHandler(final Catalog catalog, final HttpHandler aHandler) {
		this.catalog = catalog;
		this.next=aHandler;
	}

	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		final ProxyResolution resolution = resolveRequest(exchange);
		if(resolution==null) {
			System.out.printf("Accessing %s --> NOT FOUND%n",exchange.getRelativePath());
			exchange.setStatusCode(StatusCodes.NOT_FOUND);
			exchange.endExchange();
		} else if(resolution.target().isExternal()){
			System.out.printf("Accessing %s --> %s [%s] --> NOT FOUND%n",exchange.getRelativePath(),Tracing.describe(resolution),Tracing.catalogEntry(resolution.target()));
			exchange.setStatusCode(StatusCodes.NOT_FOUND);
			exchange.endExchange();
		} else {
			System.out.printf("Accessing %s --> %s [%s]%n",exchange.getRelativePath(),Tracing.describe(resolution),Tracing.catalogEntry(resolution.target()));
			Attachments.setResolution(exchange,resolution);
			Attachments.setBase(exchange,this.catalog.getBase());
			this.next.handleRequest(exchange);
		}
	}

	private ProxyResolution resolveRequest(final HttpServerExchange exchange) {
		final Builder builder=
			ProxyResolution.
				builder(targetRequestURI(exchange));
		final URI canonicalURI = canonicalURI(exchange);
		Module module=this.catalog.resolve(canonicalURI);
		if(module!=null) {
			return
				builder.
					resolved(canonicalURI).
					module(module).
					build();
		}
		final URI canonicalURIParent=canonicalURI.resolve(".");
		if(!canonicalURIParent.equals(canonicalURI)) {
			final String term=getTerm(exchange);
			if(XMLUtils.isNCName(term)) {
				module=this.catalog.resolve(canonicalURIParent);
				if(module!=null) {
					// TODO: Enforce that the term EXISTS in the module
					return
						builder.
							resolved(canonicalURIParent).
							module(module).
							fragment(term).
							build();
				}
			}
		}
		return null;
	}

	private URI targetRequestURI(final HttpServerExchange exchange) {
		return rebase(URI.create(exchange.getRequestURI()));
	}

	private URI canonicalURI(final HttpServerExchange exchange) {
		return resolve(exchange.getRelativePath().substring(1));
	}

	private String getTerm(final HttpServerExchange exchange) {
		final URI targetURI = URI.create(exchange.getRequestURI());
		final URI parentURI = targetURI.resolve(".");
		return parentURI.relativize(targetURI).toString();
	}

	private URI resolve(final String path) {
		return this.catalog.getBase().resolve(path);
	}

	private URI rebase(final URI uri) {
		return Location.rebase(this.catalog.getBase(), uri);
	}

}