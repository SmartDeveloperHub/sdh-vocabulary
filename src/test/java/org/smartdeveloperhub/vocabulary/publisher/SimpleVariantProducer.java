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

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;

import org.smartdeveloperhub.vocabulary.publisher.handlers.Variant;
import org.smartdeveloperhub.vocabulary.publisher.handlers.VariantProducer;
import org.smartdeveloperhub.vocabulary.util.Module.Format;

final class SimpleVariantProducer implements VariantProducer {

	@Override
	public Variant getVariant(final HttpServerExchange exchange) {
		final Variant variant=new Variant();
		final HeaderValues values = exchange.getRequestHeaders().get(Headers.ACCEPT);
		if(values.size()==1) {
			variant.format(fromMime(values.getFirst()));
		}
		return variant;
	}

	private static Format fromMime(final String value) {
		Format tmp=null;
		if(value.equals("application/rdf+xml")) {
			tmp=Format.RDF_XML;
		} else if(value.equals("text/turtle")) {
			tmp=Format.TURTLE;
		} else if(value.equals("application/ld+json")) {
			tmp=Format.JSON_LD;
		}
		return tmp;
	}

}