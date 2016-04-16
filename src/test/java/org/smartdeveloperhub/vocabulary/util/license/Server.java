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
package org.smartdeveloperhub.vocabulary.util.license;

import static io.undertow.Handlers.path;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

final class Server {

	private final class VocabularyProvider implements HttpHandler {
		@Override
		public void handleRequest(final HttpServerExchange exchange) throws Exception {
			final String relativePath=exchange.getRelativePath();
			final String key=relativePath.substring(1,relativePath.length());
			final String licenseId=Server.this.vocabularies.get(key);
			LOGGER.debug("Requested local vocabulary '{}' related to license '{}'...",key,licenseId);
			exchange.setStatusCode(StatusCodes.OK);
			exchange.getResponseHeaders().add(Headers.CONTENT_TYPE,"application/rdf+xml;charset=UTF-8");
			exchange.getResponseSender().send(createMockOntology(location(key),licenseId));
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(Server.class);

	private static final String NL = System.lineSeparator();

	private final int port;
	private final String host;
	private final ConcurrentMap<String,String> vocabularies=Maps.newConcurrentMap();
	private final Undertow server;

	Server(final String host, final int port) {
		this.host = host;
		this.port = port;
		this.server =
			Undertow.
				builder().
					addHttpListener(this.port,"0.0.0.0").
					setHandler(
							path().
								addPrefixPath(
									"/vocabulary/",
									new VocabularyProvider()
								)
							).
					build();
	}

	String publish(final String licenseURI) {
		final String key=String.format("%8X",licenseURI.hashCode());
		final String location = location(key);
		if(this.vocabularies.putIfAbsent(key, licenseURI)==null) {
			LOGGER.debug("Published vocabulary '{}' for license '{}' at '{}'",key,licenseURI,location);
		}
		return location;
	}

	private String location(final String key) {
		final String location = String.format("http://%s:%s/vocabulary/%s",this.host,this.port,key);
		return location;
	}

	void start() {
		this.server.start();
		LOGGER.info("Started local vocabulary provider at port {} for host {}",this.port,this.host);
	}

	void stop() {
		this.server.stop();
		LOGGER.info("Stopped local vocabulary provider at port {} for host {}",this.port,this.host);
	}

	static String createMockOntology(final String base, final String licenseURI) {
		final StringBuilder builder=new StringBuilder();
//		builder.append("<?xml version=\"1.0\"?>").append(NL);
//		builder.append("<?xml version=\"1.0\" standalone=\"yes\" encoding=\"UTF-8\"?>").append(NL);
//		builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(NL);
//		builder.append("<?xml version=\"1.0\" standalone=\"yes\"?>").append(NL);
		builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>").append(NL);
		builder.append("<rdf:RDF").append(NL);
		builder.append("  xml:base=\"").append(base).append("\"").append(NL);
		builder.append("  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"").append(NL);
		builder.append("  xmlns:owl=\"http://www.w3.org/2002/07/owl#\"").append(NL);
		builder.append("  xmlns:terms=\"http://purl.org/dc/terms/\">").append(NL);
		builder.append("    <owl:Ontology rdf:about=\"").append(base).append("\">").append(NL);
		builder.append("      <terms:license rdf:resource=\"").append(licenseURI).append("\"/>").append(NL);
		builder.append("    </owl:Ontology>").append(NL);
		builder.append("</rdf:RDF>").append(NL);
		final String vocabulary = builder.toString();
		LOGGER.trace("Vocabulary for license '{}' with base '{}':\n{}",licenseURI,base,vocabulary);
		return vocabulary;
	}

}