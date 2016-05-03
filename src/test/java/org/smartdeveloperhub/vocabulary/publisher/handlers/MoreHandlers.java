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
import java.util.concurrent.TimeUnit;

import org.smartdeveloperhub.vocabulary.util.Catalog;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

public final class MoreHandlers {

	public static final class MethodControlHandler implements HttpHandler {

		private final AllowedMethodsHandler delegate;

		private MethodControlHandler(final HttpHandler aHandler) {
			this.delegate=AllowedMethodsHandler.create(aHandler);
		}

		public MethodControlHandler allow(final HttpString method) {
			this.delegate.allow(method);
			return this;
		}

		@Override
		public void handleRequest(final HttpServerExchange exchange) throws Exception {
			this.delegate.handleRequest(exchange);
		}
	}

	private MoreHandlers() {
	}

	public static MethodControlHandler methodController(final HttpHandler aHandler) {
		return new MethodControlHandler(aHandler);
	}

	public static HttpHandler catalogReverseProxy(final Catalog catalog, final HttpHandler aHandler) {
		return new CatalogReverseProxyHandler(catalog, aHandler);
	}

	public static HttpHandler moduleReverseProxy(final Catalog catalog, final HttpHandler aHandler) {
		return new ModuleReverseProxyHandler(catalog,aHandler);
	}

	public static HttpHandler contentNegotiation(final HttpHandler aHandler, final NegotiableContent aContent) {
		return ContentNegotiationHandler.create(aHandler, aContent);
	}

	public static HttpHandler probe(final HttpHandler aHandler) {
		return new ProbeHandler(aHandler);
	}

	public static HttpHandler traceProbe(final HttpHandler aHandler, final TimeUnit unit) {
		return new ProbeTracerHandler(unit,aHandler);
	}

	public static HttpHandler temporaryRedirect(final URI location) {
		return new TemporaryRedirect(location);
	}

}