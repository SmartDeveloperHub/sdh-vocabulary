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
import java.util.Arrays;

import org.ldp4j.http.Variant;
import org.smartdeveloperhub.vocabulary.publisher.handlers.Attachments;
import org.smartdeveloperhub.vocabulary.publisher.model.Metadata;
import org.smartdeveloperhub.vocabulary.publisher.model.Owner;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.html.HtmlEscapers;

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
			final String representation=createRepresentation();
			exchange.setStatusCode(StatusCodes.OK);
			exchange.getResponseSender().send(ByteBuffer.wrap(representation.getBytes(variant.charset().charset())));
		}
	}

	@SuppressWarnings("unchecked")
	private String createRepresentation() {
		final Owner owner = new Owner();
		owner.setName("Smart Developer Hub");
		owner.setUri("http://www.smartdeveloperhub.org");
		owner.setLogo("logos/com.symbol.png");

		final Metadata meta=new Metadata();
		meta.setApplicationName("Smart Developer Hub Vocabulary Catalog");
		meta.setLanguage("en");
		meta.setDescription("Vocabularies of the Smart Developer Hub project");
		meta.getAuthors().add("Miguel Esteban Guti&eacute;rrez");
		meta.getKeywords().addAll(Arrays.asList("Smart Developer Hub","SDH","ALM","Linked Data"));


		final ImmutableMap<String, Object> ontology = ImmutableMap.<String,Object>builder().
			put("id","www.smartdeveloperhub.org.sdh").
			put("uri","http://www.smartdeveloperhub.org/sdh").
			put("title","Smart Developer Hub Ontology").
			put("domains",
				Lists.
					newArrayList(
						"ALM",
						"Application Lifecycle Management",
						"Software Engineering",
						"Linked Data")).
			put("summary",
				HtmlEscapers.
					htmlEscaper().
						escape("Abbreviated description of the 'Smart Developer Hub vocabulary'")).
			put("description",
				HtmlEscapers.
					htmlEscaper().
						escape("Quite a long description of the 'Smart Developer Hub vocabulary'")).
			put("licenses",
				Lists.
					newArrayList(
						ImmutableMap.<String,Object>builder().
							put("uri","http://purl.org/NET/rdflicense/cc-by-nc-sa2.0").
							put("label","CC-BY-NC-SA").
							build()
					)).
			put("languages",
				Lists.
					newArrayList(
						ImmutableMap.<String,Object>builder().
							put("uri","http://lexvo.org/id/iso639-3/eng").
							put("label","en").
							build()
					)).
			build();
		final ImmutableMap<String, Object> context=
			ImmutableMap.<String,Object>builder().
				put("publication",
					ImmutableMap.<String,Object>builder().
						put("title","www.smartdeveloperhub.org").
						put("date","April, 2016").
						put("copyright","Center for Open Middleware").
						put("tags",Lists.newArrayList("Smart Developer Hub","SDH","ALM","Linked Data")).
						put("metadata",meta).
						put("owner",owner).
						put("ontologies",
							Lists.newArrayList(
								ontology,
								ontology,
								ontology,
								ontology,
								ontology,
								ontology,
								ontology,
								ontology
							)).
						build()).
				build();
		return Templates.catalogRepresentation(context);
	}

}