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
package org.smartdeveloperhub.vocabulary.publisher;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.ldp4j.http.Variant;
import org.smartdeveloperhub.vocabulary.publisher.handlers.Attachments;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

final class CatalogRepresentionGenerator implements HttpHandler {

	CatalogRepresentionGenerator() {
		// Package-private
	}

	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		if(!Strings.isNullOrEmpty(exchange.getQueryString())) {
			exchange.setStatusCode(StatusCodes.BAD_REQUEST);
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,"text/plain; charset=\"UTF-8\"");
			exchange.getResponseSender().send("Queries not allowed");
		} else {
			final Variant variant=Attachments.getVariant(exchange);
//			final String representation="<html><head><body>TODO: Generate VOCAB</body></head></html>";
			final String representation=generate();
			exchange.setStatusCode(StatusCodes.OK);
			exchange.getResponseSender().send(ByteBuffer.wrap(representation.getBytes(variant.charset().charset())));
		}
	}

	@SuppressWarnings("unchecked")
	private String generate() {
		try {
			final Jinjava jinjava = new Jinjava();
			final ImmutableMap<String, Object> context=
				ImmutableMap.<String,Object>builder().
					put("publication",
						ImmutableMap.<String,Object>builder().
							put("title","www.smartdeveloperhub.org").
							put("date","April, 2016").
							put("copyright","Center for Open Middleware").
							put("tags",Lists.newArrayList("tag1","tag2")).
							put("meta",
								ImmutableMap.<String,Object>builder().
									put("description","www.smartdeveloperhub.org").
									put("language","en").
									put("keywords","ALM, Linked Data, RDF, OWL").
									put("author","Miguel Esteban Guti&eacute;rrez").
									build()).
							put("owner",
								ImmutableMap.<String,Object>builder().
									put("uri","http://www.smartdeveloperhub.org").
									put("name","Smart Developer Hub").
									put("logo","logo.gif").
									build()).
							put("ontologies",
								Lists.newArrayList(
									ImmutableMap.<String,Object>builder().
										put("uri","http://www.smartdeveloperhub.org/sdh").
										put("title","Smart Developer Hub Ontology").
										put("id","www.smartdeveloperhub.org.sdh").
										put("license",
											ImmutableMap.<String,Object>builder().
												put("uri","http://purl.org/NET/rdflicense/cc-by-nc-sa2.0").
												put("label","CC-BY-NC-SA").
												build()).
										put("languages",
											Lists.
												newArrayList(
													ImmutableMap.<String,Object>builder().
														put("uri","http://lexvo.org/id/iso639-3/eng").
														put("label","en").
														build()
												)).
										put("domains",Lists.newArrayList("ALM","Application Lifecycle Management","Software Engineering","Linked Data")).
										put("description",
											ImmutableMap.<String,Object>builder().
												put("text","Quite a long description of the Smart Developer Hub vocabulary").
												put("abstract","Abbreviated description of the Smart Developer Hub vocabulary").
												build()).
										build()
								)).
							build()).
					build();
			final String template=
				Resources.
					toString(
						Resources.getResource("templates/vocab.html"),
						StandardCharsets.UTF_8);
			return jinjava.render(template, context);
		} catch(final Exception e) {
			System.err.println(e);
			return "<html><head></head><body><h1>OOPS! Something went wrong...</h1><pre>"+Throwables.getStackTraceAsString(e)+"</pre></body></html>";
		}
	}

}