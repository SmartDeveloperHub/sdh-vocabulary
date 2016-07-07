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
package org.smartdeveloperhub.vocabulary.publisher;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.vocabulary.publisher.handlers.Attachments;
import org.smartdeveloperhub.vocabulary.publisher.handlers.ProxyResolution;
import org.smartdeveloperhub.vocabulary.publisher.spi.DocumentationDeploymentFactory;
import org.smartdeveloperhub.vocabulary.publisher.util.Location;
import org.smartdeveloperhub.vocabulary.publisher.util.Tracing;
import org.smartdeveloperhub.vocabulary.util.Module;

import com.google.common.base.Strings;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

final class ModuleDocumentationRedirector implements HttpHandler {

	private static final Logger LOGGER=LoggerFactory.getLogger(ModuleDocumentationRedirector.class);

	private final DocumentationDeploymentFactory deploymentFactory;

	ModuleDocumentationRedirector(final DocumentationDeploymentFactory factory) {
		this.deploymentFactory = factory;
	}

	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		if(!Strings.isNullOrEmpty(exchange.getQueryString())) {
			exchange.setStatusCode(StatusCodes.BAD_REQUEST);
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,"text/plain; charset=\"UTF-8\"");
			exchange.getResponseSender().send("Queries not allowed");
			return;
		}
		final URI location=getDocumentationLocation(exchange);
		final URI relativeLocation=relativize(exchange, location);
		exchange.setStatusCode(StatusCodes.SEE_OTHER);
		exchange.getResponseHeaders().clear();
		exchange.getResponseHeaders().add(Headers.LOCATION,relativeLocation.toString());
	}

	private URI getDocumentationLocation(final HttpServerExchange exchange) {
		final ProxyResolution resolution = Attachments.getResolution(exchange);
		final Module module=resolution.target();
		final URI result = this.deploymentFactory.create(module).implementationLandingPage();
		LOGGER.debug("Redirecting {} ({}) to {}",Tracing.describe(resolution),Tracing.catalogEntry(module),result);
		return result;
	}

	private URI relativize(final HttpServerExchange exchange, final URI location) {
		final ProxyResolution resolution = Attachments.getResolution(exchange);
		URI result =
			Location.relativize(
				resolution.target().context().base(),
				URI.create(exchange.getRequestURI()),
				location);
		if(resolution.isFragment()) {
			result=result.resolve("#"+resolution.fragment());
		}
		LOGGER.trace("Effective path to {} is {}",location,result);
		return result;
	}
}