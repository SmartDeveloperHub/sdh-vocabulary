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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.ldp4j.http.Alternative;
import org.ldp4j.http.Alternatives;
import org.ldp4j.http.CharacterEncoding;
import org.ldp4j.http.ContentNegotiation;
import org.ldp4j.http.ContentNegotiator;
import org.ldp4j.http.Language;
import org.ldp4j.http.MediaType;
import org.ldp4j.http.Negotiable;
import org.ldp4j.http.NegotiationResult;
import org.ldp4j.http.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;

public final class ContentNegotiationHandler implements HttpHandler {

	private static final class Scenario {

		private final HttpHandler next;
		private final NegotiableContent content;

		private Scenario(final HttpHandler next, final NegotiableContent content) {
			this.next = next;
			this.content = content;
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(ContentNegotiationHandler.class);

	private final List<Scenario> scenarios;
	private NegotiableContent content;

	private ContentNegotiationHandler() {
		this.scenarios=Lists.newLinkedList();
		this.content=NegotiableContent.newInstance();
	}

	public ContentNegotiationHandler negotiate(final NegotiableContent aContent,final HttpHandler aHandler) {
		Preconditions.checkArgument(aContent.hasTypes(),"Negotiable contents must define types (%s)",aContent);
		final List<MediaType> clashes=this.content.clashingMediaTypes(aContent);
		Preconditions.checkArgument(clashes.isEmpty(),"Some media types are already associated to another handler (%s)",Joiner.on(", ").join(clashes));
		this.content=this.content.merge(aContent);
		this.scenarios.add(new Scenario(aHandler,aContent));
		return this;
	}

	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		if(this.scenarios.isEmpty()) {
			failDispatch(exchange,"Cannot handle request: no content negotiation scenarios defined");
			return;
		}
		LOGGER.debug("Starting content negotiation...");
		final ContentNegotiator negotiator = defaultNegotiator();
		final List<Failure> failures = addAcceptanceRequirements(exchange, negotiator);
		if(!failures.isEmpty()) {
			abortNegotation(exchange, failures);
			return;
		}

		final NegotiationResult negotiation=negotiator.negotiate();
		if(!negotiation.isAcceptable()) {
			failNegotiation(exchange, negotiation);
			return;
		}

		final Variant variant = negotiation.variant();
		for(final Scenario scenario:this.scenarios) {
			if(scenario.content.isAcceptable(variant)) {
				forwardRequestHandling(exchange, negotiation, scenario.next);
				return;
			}
		}
		failDispatch(exchange,"Could not dispatch variant %s",variant);
	}

	private void failDispatch(final HttpServerExchange exchange, final String format, final Object... args) {
		exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,"text/plain; charset=\"UTF-8\"");
		final String message=String.format(format, args);
		exchange.getResponseSender().send(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
		LOGGER.debug(message);
	}

	private void forwardRequestHandling(final HttpServerExchange exchange, final NegotiationResult negotiation, final HttpHandler next) throws Exception {
		if(next!=null) {
			final Variant variant = negotiation.variant();
			Attachments.setVariant(exchange, variant);
			logAcceptance(variant);
			addContentNegotiationHeaders(exchange,negotiation,true);
			next.handleRequest(exchange);
		}
	}

	private void logAcceptance(final Variant variant) {
		LOGGER.debug("Accepted:");
		printAttribute("Media type", variant.type());
		printAttribute("Character encoding", variant.charset());
		printAttribute("Language", variant.language());
	}

	private void failNegotiation(final HttpServerExchange exchange, final NegotiationResult negotiation) {
		exchange.setStatusCode(StatusCodes.NOT_ACCEPTABLE);
		addContentNegotiationHeaders(exchange, negotiation, false);
		final URI canonicalURI = HandlerUtil.canonicalURI(exchange);
		final String message = acceptableResources(canonicalURI,negotiation.alternatives());
		exchange.getResponseSender().send(message);
		LOGGER.debug("Not acceptable:\n"+message);
	}

	private void addContentNegotiationHeaders(final HttpServerExchange exchange, final NegotiationResult negotiation, final boolean accepted) {
		final HeaderMap headers = exchange.getResponseHeaders();
		for(final Entry<String, Collection<String>> entry:negotiation.responseHeaders(accepted).asMap().entrySet()) {
			headers.add(
				HttpString.tryFromString(entry.getKey()),
				Joiner.on(", ").join(entry.getValue()));
		}
	}

	private void abortNegotation(final HttpServerExchange exchange, final List<Failure> failures) {
		exchange.setStatusCode(StatusCodes.BAD_REQUEST);
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,"text/plain; charset=\"UTF-8\"");
		final String message=errorMessage(failures);
		exchange.getResponseSender().send(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
		LOGGER.debug(message);
	}

	private ContentNegotiator defaultNegotiator() {
		ContentNegotiator negotiator=ContentNegotiator.newInstance();
		for(final MediaType value:this.content.mediaTypes()) {
			negotiator=negotiator.support(value);
		}
		for(final CharacterEncoding value:this.content.characterEncodings()) {
			negotiator=negotiator.support(value);
		}
		for(final Language value:this.content.languages()) {
			negotiator=negotiator.support(value);
		}
		return negotiator;
	}

	private <T extends Negotiable> void printAttribute(final String name, final T attribute) {
		if(attribute!=null) {
			LOGGER.debug("- {} : {}",name,attribute.toHeader());
		}
	}

	private String errorMessage(final List<Failure> failures) {
		final StringBuilder builder=new StringBuilder("Could not negotiate content due to the following errors:");
		for(final Failure failure:failures) {
			builder.append(System.lineSeparator()).append("- ").append(failure.toString().replaceAll("\n", "\n  "));
		}
		return builder.toString();
	}

	private List<Failure> addAcceptanceRequirements(final HttpServerExchange exchange, final ContentNegotiator negotiator) {
		final List<Failure> failures=Lists.newLinkedList();
		final HeaderMap headers = exchange.getRequestHeaders();
		addAcceptHeaders(negotiator, failures, headers);
		addAcceptCharsetHeaders(negotiator, failures, headers);
		addAcceptLanguageHeaders(negotiator, failures, headers);
		return failures;
	}

	private void addAcceptLanguageHeaders(final ContentNegotiator negotiator, final List<Failure> failures, final HeaderMap headers) {
		for(final String value:headers(headers,ContentNegotiation.ACCEPT_LANGUAGE)) {
			try {
				negotiator.acceptLanguage(value);
				LOGGER.debug("- Accept-Language: {}",value);
			} catch(final IllegalArgumentException e) {
				failures.add(Failure.create(ContentNegotiation.ACCEPT_LANGUAGE,value,e));
			}
		}
	}

	private void addAcceptCharsetHeaders(final ContentNegotiator negotiator, final List<Failure> failures, final HeaderMap headers) {
		for(final String value:headers(headers,ContentNegotiation.ACCEPT_CHARSET)) {
			try {
				negotiator.acceptCharset(value);
				LOGGER.debug("- Accept-Charset: {}",value);
			} catch(final IllegalArgumentException e) {
				failures.add(Failure.create(ContentNegotiation.ACCEPT_CHARSET,value,e));
			}
		}
	}

	private void addAcceptHeaders(final ContentNegotiator negotiator, final List<Failure> failures, final HeaderMap headers) {
		for(final String value:headers(headers,ContentNegotiation.ACCEPT)) {
			try {
				negotiator.accept(value);
				LOGGER.debug("- Accept: {}",value);
			} catch(final IllegalArgumentException e) {
				failures.add(Failure.create(ContentNegotiation.ACCEPT,value,e));
			}
		}
	}

	private List<String> headers(final HeaderMap headers, final String accept) {
		final List<String> values=Lists.newArrayList();
		final HeaderValues headerValues = headers.get(accept);
		if(headerValues!=null) {
			for(final String value:headerValues) {
				final String[] split = value.split(",");
				for(final String part:split) {
					values.add(part);
				}
			}
		}
		return values;
	}

	private String acceptableResources(final URI canonicalURI, final Alternatives alternatives ) {
		final StringBuilder builder=new StringBuilder();
		builder.append(canonicalURI);
		for(final Alternative alternative:alternatives) {
			builder.append(System.lineSeparator()).append("  - ").append(alternative);
		}
		return builder.toString();
	}

	static ContentNegotiationHandler newInstance() {
		return new ContentNegotiationHandler();
	}

}