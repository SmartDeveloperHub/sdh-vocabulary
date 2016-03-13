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

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

import java.net.URI;

import org.smartdeveloperhub.vocabulary.util.Module.Format;

final class ContentNegotiationHandler implements HttpHandler {

	private final VariantProducer negotiator;
	private final HttpHandler next;

	ContentNegotiationHandler(final VariantProducer producer, final HttpHandler next) {
		this.negotiator = producer;
		this.next = next;
	}

	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		final String ext = HandlerUtil.getExtension(exchange.getRelativePath());
		final Variant variant = this.negotiator.getVariant(exchange);
		if(variant.format()==null) {
			variant.format(Format.fromExtension(ext));
		}
		if(isAcceptable(variant)) {
			exchange.setStatusCode(StatusCodes.NOT_ACCEPTABLE);
			exchange.getResponseHeaders().put(Headers.VARY,Headers.ACCEPT_STRING);
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,"text/plain; charset=\"UTF-8\"");
			exchange.getResponseSender().send(acceptableResources(exchange));
		} else {
			Attachments.setVariant(exchange, variant);
			this.next.handleRequest(exchange);
		}
	}

	private boolean isAcceptable(final Variant variant) {
		return variant.format()==null;
	}

	private String acceptableResources(final HttpServerExchange exchange) {
		final URI canonicalURI = HandlerUtil.canonicalURI(exchange);
		final StringBuilder builder=new StringBuilder();
		builder.append("text/turtle").append(" : ").append(canonicalURI).append(System.lineSeparator());
		builder.append("application/rdf+xml").append(" : ").append(canonicalURI).append(System.lineSeparator());
		builder.append("application/ld+json").append(" : ").append(canonicalURI).append(System.lineSeparator());
		return builder.toString();
	}

}